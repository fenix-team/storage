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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class RedisModelRepository<ModelType extends Model, Reader extends ModelReader<Reader, JsonObject>>
  extends AbstractAsyncModelRepository<ModelType> {

  protected final Gson gson;
  protected final Function<JsonObject, Reader> readerFactory;
  protected final ModelCodec.Writer<ModelType, JsonObject> writer;
  protected final ModelCodec.Reader<ModelType, JsonObject, Reader> reader;
  protected final JedisPool jedisPool;
  protected final String tableName;
  protected final int expireAfterSave;
  protected final int expireAfterAccess;

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

  @Contract(value = " -> new")
  public static <T extends Model, Reader extends ModelReader<Reader, JsonObject>>
  @NotNull RedisModelRepositoryBuilder<T, Reader> builder() {
    return new RedisModelRepositoryBuilder<>();
  }

  @Override
  public @NotNull ModelType saveSync(final @NotNull ModelType model) {
    try (final Jedis jedis = this.jedisPool.getResource()) {
      final JsonObject object = this.writer.serialize(model);
      final Map<String, String> map = new HashMap<>(object.size());
      for (final Map.Entry<String, JsonElement> entry : object.entrySet()) {
        map.put(entry.getKey(), gson.toJson(entry.getValue()));
      }
      final String key = this.tableName + ":" + model.getId();
      jedis.hset(key, map);
      if (this.expireAfterSave > 0) {
        jedis.expire(key, this.expireAfterSave);
      }
      return model;
    }
  }

  @Override
  public boolean deleteSync(final @NotNull String id) {
    try (final Jedis jedis = this.jedisPool.getResource()) {
      return jedis.del(this.tableName + ":" + id) > 0;
    }
  }

  @Override
  public @Nullable ModelType findSync(final @NotNull String id) {
    try (final Jedis jedis = jedisPool.getResource()) {
      final String key = this.tableName + ":" + id;
      return this.readModel(jedis, key);
    }
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findSync(
    final @NotNull String field,
    final @NotNull String value,
    final @NotNull Function<Integer, C> factory
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
    try (final Jedis jedis = this.jedisPool.getResource()) {
      final Set<String> keys = jedis.keys(this.tableName + ":*");

      if (keys == null || keys.isEmpty()) {
        return null;
      }

      final List<String> result = new ArrayList<>(keys.size());

      for (final String key : keys) {
        result.add(key.substring(this.tableName.length() + 1));
      }

      return result;
    }
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    try (final Jedis jedis = this.jedisPool.getResource()) {
      final Set<String> keys = jedis.keys(this.tableName + ":*");

      if (keys == null || keys.isEmpty()) {
        return null;
      }

      final C foundModels = factory.apply(keys.size());

      for (final String key : keys) {
        final ModelType model = this.readModel(jedis, key);

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
  public boolean existsSync(final @NotNull String id) {
    try (final Jedis jedis = this.jedisPool.getResource()) {
      return jedis.exists(this.tableName + ":" + id);
    }
  }

  protected @Nullable ModelType readModel(final @NotNull Jedis jedis, final @NotNull String key) {
    final Map<String, String> map = jedis.hgetAll(key);

    if (map.isEmpty()) {
      return null;
    }

    if (this.expireAfterAccess > 0) {
      jedis.expire(key, this.expireAfterAccess);
    }

    final JsonObject object = new JsonObject();

    for (final Map.Entry<String, String> entry : map.entrySet()) {
      object.add(entry.getKey(), this.gson.fromJson(entry.getValue(), JsonElement.class));
    }

    return this.reader.deserialize(this.readerFactory.apply(object));
  }
}
