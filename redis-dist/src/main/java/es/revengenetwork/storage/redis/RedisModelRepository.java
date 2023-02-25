package es.revengenetwork.storage.redis;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.repository.AbstractAsyncModelRepository;
import es.revengenetwork.storage.repository.ModelRepository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public class RedisModelRepository<ModelType extends Model, Reader extends ModelReader<Reader,
                                                                                       JsonObject>>
  extends AbstractAsyncModelRepository<ModelType> {

  private final Gson gson;
  private final Function<JsonObject, Reader> readerFactory;
  private final ModelCodec.Writer<ModelType, JsonObject> writer;
  private final ModelCodec.Reader<ModelType, JsonObject, Reader> reader;
  private final JedisPool jedisPool;
  private final String tableName;
  private final int expireAfterSave;
  private final int expireAfterAccess;

  protected RedisModelRepository(
    final @NotNull Executor executor,
    final @NotNull Gson gson,
    final @NotNull Function<JsonObject, Reader> readerFactory,
    final @NotNull ModelCodec.Writer<ModelType, JsonObject> writer,
    final @NotNull ModelCodec.Reader<ModelType, JsonObject, Reader> reader,
    final @NotNull JedisPool jedisPool,
    final @NotNull String tableName,
    final int expireAfterSave,
    final int expireAfterAccess
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
  @NotNull RedisModelRepositoryBuilder<T, Reader> builder(
    @NotNull Class<T> type,
    @NotNull Class<Reader> ignoredReaderType
  ) {
    return new RedisModelRepositoryBuilder<>(type);
  }

  @Override
  public void saveSync(@NotNull ModelType model) {
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
  public @Nullable ModelType findSync(@NotNull String id) {
    try (Jedis jedis = jedisPool.getResource()) {
      String key = tableName + ":" + id;
      return readModel(jedis, key);
    }
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findSync(
    @NotNull final String field,
    @NotNull final String value,
    @NotNull final Function<Integer, C> factory
  ) {
    if (!field.equals(ModelRepository.ID_FIELD)) {
      throw new IllegalArgumentException("Only ID field is supported for JSON find");
    }

    final ModelType model = this.findSync(value);

    if (model == null) {
      return null;
    }

    final C collection = factory.apply(1);
    collection.add(model);
    return collection;
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    try (Jedis jedis = jedisPool.getResource()) {
      Set<String> keys = jedis.keys(tableName + ":*");

      if (keys == null || keys.isEmpty()) {
        return Collections.emptyList();
      }

      List<String> result = new ArrayList<>(keys.size());

      for (String key : keys) {
        result.add(key.substring(tableName.length() + 1));
      }

      return result;
    }
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    @NotNull final Consumer<ModelType> postLoadAction,
    @NotNull final Function<Integer, C> factory
  ) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      Set<String> keys = jedis.keys(this.tableName + ":*");

      if (keys == null || keys.isEmpty()) {
        return null;
      }

      C foundModels = factory.apply(keys.size());

      for (String key : keys) {
        ModelType model = readModel(jedis, key);

        if (model == null) {
          continue;
        }

        postLoadAction.accept(model);
        foundModels.add(model);
      }

      return foundModels;
    }
  }

  @Override
  public boolean existsSync(@NotNull final String id) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.exists(tableName + ":" + id);
    }
  }

  private @Nullable ModelType readModel(@NotNull Jedis jedis, @NotNull String key) {
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
