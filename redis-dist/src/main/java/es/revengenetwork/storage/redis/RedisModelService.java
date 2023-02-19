package es.revengenetwork.storage.redis;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import es.revengenetwork.storage.dist.RemoteModelService;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public class RedisModelService<T extends Model, Reader extends ModelReader<Reader, JsonObject>>
  extends RemoteModelService<T> {

  private final Gson gson;
  private final Function<JsonObject, Reader> readerFactory;
  private final ModelCodec.Writer<T, JsonObject> writer;
  private final ModelCodec.Reader<T, JsonObject, Reader> reader;
  private final JedisPool jedisPool;
  private final String tableName;
  private final int expireAfterSave;
  private final int expireAfterAccess;

  protected RedisModelService(
    @NotNull Executor executor,
    @NotNull Gson gson,
    @NotNull Function<JsonObject, Reader> readerFactory,
    @NotNull ModelCodec.Writer<T, JsonObject> writer,
    @NotNull ModelCodec.Reader<T, JsonObject, Reader> reader,
    @NotNull JedisPool jedisPool,
    @NotNull String tableName,
    int expireAfterSave,
    int expireAfterAccess
  ) {
    super(executor);
    this.gson = gson;
    this.readerFactory = readerFactory;
    this.writer = writer;
    this.reader = reader;
    this.jedisPool = jedisPool;
    this.tableName = tableName;
    this.expireAfterSave = expireAfterSave;
    this.expireAfterAccess = expireAfterAccess;
  }

  @Contract(pure = true, value = "_, _ -> new")
  public static <T extends Model, Reader extends ModelReader<Reader, JsonObject>>
  @NotNull RedisModelServiceBuilder<T, Reader> builder(
    @NotNull Class<T> type,
    @NotNull Class<Reader> ignoredReaderType
  ) {
    return new RedisModelServiceBuilder<>(type);
  }

  @Override
  public void saveSync(@NotNull T model) {
    try (Jedis jedis = jedisPool.getResource()) {
      JsonObject object = writer.serialize(model);
      Map<String, String> map = new HashMap<>(object.size());

      for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
        map.put(entry.getKey(), gson.toJson(entry.getValue()));
      }

      String key = tableName + ":" + model.getId();
      jedis.hset(key, map);

      if (expireAfterSave > 0) {
        jedis.expire(key, expireAfterSave);
      }
    }
  }

  @Override
  public boolean deleteSync(@NotNull String id) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.del(tableName + ":" + id) > 0;
    }
  }

  @Override
  public @Nullable T findSync(@NotNull String id) {
    try (Jedis jedis = jedisPool.getResource()) {
      String key = tableName + ":" + id;
      return readModel(jedis, key);
    }
  }

  @Override
  public List<T> findSync(@NotNull String field, @NotNull String value) {
    if (!field.equals(ModelService.ID_FIELD)) {
      throw new IllegalArgumentException(
        "Only ID field is supported for sync find"
      );
    }

    return Collections.singletonList(findSync(value));
  }

  @Override
  public List<T> findAllSync(@NotNull Consumer<T> postLoadAction) {
    try (Jedis jedis = jedisPool.getResource()) {
      Set<String> keys = jedis.keys(tableName + ":*");

      if (keys == null || keys.isEmpty()) {
        return Collections.emptyList();
      }

      List<T> result = new ArrayList<>(keys.size());

      for (String key : keys) {
        T model = readModel(jedis, key);

        if (model == null) {
          continue;
        }

        postLoadAction.accept(model);
        result.add(model);
      }

      return result;
    }
  }

  private @Nullable T readModel(@NotNull Jedis jedis, @NotNull String key) {
    Map<String, String> map = jedis.hgetAll(key);

    if (map.isEmpty()) {
      return null;
    }

    if (expireAfterAccess > 0) {
      jedis.expire(key, expireAfterAccess);
    }

    JsonObject object = new JsonObject();

    for (Map.Entry<String, String> entry : map.entrySet()) {
      object.add(entry.getKey(), gson.fromJson(entry.getValue(), JsonElement.class));
    }

    return reader.deserialize(readerFactory.apply(object));
  }
}
