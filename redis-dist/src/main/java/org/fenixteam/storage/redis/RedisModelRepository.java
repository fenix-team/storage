package org.fenixteam.storage.redis;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import org.fenixteam.storage.codec.ModelDeserializer;
import org.fenixteam.storage.codec.ModelSerializer;
import org.fenixteam.storage.model.Model;
import org.fenixteam.storage.repository.AbstractAsyncModelRepository;
import org.fenixteam.storage.repository.ModelRepository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@SuppressWarnings("unused")
public class RedisModelRepository<ModelType extends Model> extends AbstractAsyncModelRepository<ModelType> {
  protected final ModelSerializer<ModelType, JsonObject> modelSerializer;
  protected final ModelDeserializer<ModelType, JsonObject> modelDeserializer;
  protected final JedisPool jedisPool;
  protected final String tableName;
  protected final int expireAfterSave;
  protected final int expireAfterAccess;

  protected RedisModelRepository(
    final @NotNull Executor executor,
    final @NotNull ModelSerializer<ModelType, JsonObject> modelSerializer,
    final @NotNull ModelDeserializer<ModelType, JsonObject> modelDeserializer,
    final @NotNull JedisPool jedisPool,
    final @NotNull String tableName,
    final int expireAfterSave,
    final int expireAfterAccess
  ) {
    super(executor);
    this.modelSerializer = modelSerializer;
    this.modelDeserializer = modelDeserializer;
    this.jedisPool = jedisPool;
    this.tableName = tableName;
    this.expireAfterSave = expireAfterSave;
    this.expireAfterAccess = expireAfterAccess;
  }

  @Contract(value = " -> new")
  public static <T extends Model> @NotNull RedisModelRepositoryBuilder<T> builder() {
    return new RedisModelRepositoryBuilder<>();
  }

  @Override
  public @NotNull ModelType saveSync(final @NotNull ModelType model) {
    try (final var jedis = this.jedisPool.getResource()) {
      final var object = this.modelSerializer.serialize(model);
      final var map = new HashMap<String, String>(object.size());
      for (final var entry : object.entrySet()) {
        final var stringWriter = new StringWriter();
        try (final var writer = new JsonWriter(stringWriter)) {
          writer.setSerializeNulls(false);
          TypeAdapters.JSON_ELEMENT.write(writer, entry.getValue());
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
        map.put(entry.getKey(), stringWriter.toString());
      }
      final var key = this.tableName + ":" + model.id();
      jedis.hset(key, map);
      if (this.expireAfterSave > 0) {
        jedis.expire(key, this.expireAfterSave);
      }
      return model;
    }
  }

  @Override
  public boolean deleteSync(final @NotNull String id) {
    try (final var jedis = this.jedisPool.getResource()) {
      return jedis.del(this.tableName + ":" + id) > 0;
    }
  }

  @Override
  public @Nullable ModelType findSync(final @NotNull String id) {
    try (final var jedis = this.jedisPool.getResource()) {
      final var key = this.tableName + ":" + id;
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
    final var model = this.findSync(value);
    if (model == null) {
      return null;
    }
    final var collection = factory.apply(1);
    collection.add(model);
    return collection;
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    try (final var jedis = this.jedisPool.getResource()) {
      final var keys = jedis.keys(this.tableName + ":*");
      if (keys == null || keys.isEmpty()) {
        return null;
      }
      final var result = new ArrayList<String>(keys.size());
      for (final var key : keys) {
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
    try (final var jedis = this.jedisPool.getResource()) {
      final var keys = jedis.keys(this.tableName + ":*");
      if (keys == null || keys.isEmpty()) {
        return null;
      }
      final var foundModels = factory.apply(keys.size());
      for (final var key : keys) {
        final var model = this.readModel(jedis, key);
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
    try (final var jedis = this.jedisPool.getResource()) {
      return jedis.exists(this.tableName + ":" + id);
    }
  }

  protected @Nullable ModelType readModel(final @NotNull Jedis jedis, final @NotNull String key) {
    final var map = jedis.hgetAll(key);
    if (map.isEmpty()) {
      return null;
    }
    if (this.expireAfterAccess > 0) {
      jedis.expire(key, this.expireAfterAccess);
    }
    final var jsonObject = new JsonObject();
    for (final var entry : map.entrySet()) {
      try (final var reader = new JsonReader(new StringReader(entry.getValue()))) {
        jsonObject.add(entry.getKey(), TypeAdapters.JSON_ELEMENT.read(reader));
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
    return this.modelDeserializer.deserialize(jsonObject);
  }
}
