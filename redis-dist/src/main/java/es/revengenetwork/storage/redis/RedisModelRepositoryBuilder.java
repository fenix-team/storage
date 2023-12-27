package es.revengenetwork.storage.redis;

import com.google.gson.JsonObject;
import es.revengenetwork.storage.codec.ModelDeserializer;
import es.revengenetwork.storage.codec.ModelSerializer;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.repository.AsyncModelRepository;
import es.revengenetwork.storage.repository.builder.AbstractModelRepositoryBuilder;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPool;

@SuppressWarnings("unused")
public class RedisModelRepositoryBuilder<ModelType extends Model>
  extends AbstractModelRepositoryBuilder<ModelType> {
  private String tableName;
  private int expireAfterSave;
  private int expireAfterAccess;
  private JedisPool jedisPool;
  private ModelSerializer<ModelType, JsonObject> modelSerializer;
  private ModelDeserializer<ModelType, JsonObject> modelDeserializer;

  protected RedisModelRepositoryBuilder() {
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType> tableName(final @NotNull String tableName) {
    this.tableName = tableName;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType> expireAfterSave(final int expireAfterSave) {
    this.expireAfterSave = expireAfterSave;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType> expireAfterAccess(final int expireAfterAccess) {
    this.expireAfterAccess = expireAfterAccess;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType> jedisPool(final @NotNull JedisPool jedisPool) {
    this.jedisPool = jedisPool;
    return this;
  }

  @Contract("_ -> this")
  public RedisModelRepositoryBuilder<ModelType> modelDeserializer(
    final @NotNull ModelDeserializer<ModelType, JsonObject> modelDeserializer
  ) {
    this.modelDeserializer = modelDeserializer;
    return this;
  }

  @Contract("_ -> this")
  public RedisModelRepositoryBuilder<ModelType> modelSerializer(
    final @NotNull ModelSerializer<ModelType, JsonObject> modelSerializer
  ) {
    this.modelSerializer = modelSerializer;
    return this;
  }

  @Contract("_ -> new")
  public @NotNull AsyncModelRepository<ModelType> build(final @NotNull Executor executor) {
    if (this.expireAfterSave <= 0) {
      this.expireAfterSave = -1;
    }
    if (this.expireAfterAccess <= 0) {
      this.expireAfterAccess = -1;
    }
    return new RedisModelRepository<>(
      executor,
      this.modelSerializer,
      this.modelDeserializer,
      this.jedisPool,
      this.tableName,
      this.expireAfterSave,
      this.expireAfterAccess);
  }
}
