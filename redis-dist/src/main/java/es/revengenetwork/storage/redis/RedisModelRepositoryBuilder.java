package es.revengenetwork.storage.redis;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.revengenetwork.storage.repository.ModelRepository;
import es.revengenetwork.storage.repository.builder.LayoutModelRepositoryBuilder;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import es.revengenetwork.storage.repository.CachedModelRepository;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPool;

import java.util.function.Function;

public class RedisModelRepositoryBuilder
  <ModelType extends Model, Reader extends ModelReader<Reader, JsonObject>>
  extends LayoutModelRepositoryBuilder<ModelType, RedisModelRepositoryBuilder<ModelType, Reader>> {

  private Gson gson;
  private String tableName;
  private int expireAfterSave;
  private int expireAfterAccess;
  private JedisPool jedisPool;
  private Function<JsonObject, Reader> readerFactory;
  private ModelCodec.Writer<ModelType, JsonObject> writer;
  private ModelCodec.Reader<ModelType, JsonObject, Reader> reader;

  protected RedisModelRepositoryBuilder(@NotNull Class<ModelType> type) {
    super(type);
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType, Reader> gson(@NotNull Gson gson) {
    this.gson = gson;
    return back();
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType, Reader> tableName(@NotNull String tableName) {
    this.tableName = tableName;
    return back();
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType, Reader> expireAfterSave(int expireAfterSave) {
    this.expireAfterSave = expireAfterSave;
    return back();
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType, Reader> expireAfterAccess(int expireAfterAccess) {
    this.expireAfterAccess = expireAfterAccess;
    return back();
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType, Reader> jedisPool(@NotNull JedisPool jedisPool) {
    this.jedisPool = jedisPool;
    return back();
  }

  @Contract("_ -> this")
  public RedisModelRepositoryBuilder<ModelType, Reader> modelReader(
    @NotNull ModelCodec.Reader<ModelType, JsonObject
                                , Reader> reader
  ) {
    this.reader = reader;
    return back();
  }

  @Contract("_ -> this")
  public RedisModelRepositoryBuilder<ModelType, Reader> modelWriter(@NotNull ModelCodec.Writer<ModelType, JsonObject> writer) {
    this.writer = writer;
    return back();
  }

  @Contract("_ -> this")
  public RedisModelRepositoryBuilder<ModelType, Reader> readerFactory(@NotNull Function<JsonObject, Reader> readerFactory) {
    this.readerFactory = readerFactory;
    return back();
  }

  @Contract(" -> this")
  @Override
  protected @NotNull RedisModelRepositoryBuilder<ModelType, Reader> back() {
    return this;
  }

  @Override
  public ModelRepository<ModelType> build() {
    check();

    if (expireAfterSave <= 0) {
      expireAfterSave = -1;
    }

    if (expireAfterAccess <= 0) {
      expireAfterAccess = -1;
    }

    ModelRepository<ModelType> modelRepository = new RedisModelRepository<>(
      executor, gson, readerFactory, writer, reader, jedisPool,
      tableName, expireAfterSave, expireAfterAccess);

    if (cacheModelRepository == null) {
      return modelRepository;
    } else {
      return new CachedModelRepository<>(executor, cacheModelRepository, modelRepository);
    }
  }
}
