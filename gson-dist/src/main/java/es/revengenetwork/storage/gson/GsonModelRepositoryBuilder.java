package es.revengenetwork.storage.gson;

import com.google.gson.JsonObject;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.repository.AsyncModelRepository;
import es.revengenetwork.storage.repository.CachedModelRepository;
import es.revengenetwork.storage.repository.ModelRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class GsonModelRepositoryBuilder<ModelType extends Model, Reader extends ModelReader<JsonObject>> {
  private final Class<ModelType> modelType;
  private Path folderPath;
  private boolean prettyPrinting;
  private ModelCodec.Writer<ModelType, JsonObject> writer;
  private Function<JsonObject, Reader> readerFactory;
  private ModelCodec.Reader<ModelType, JsonObject, Reader> reader;

  GsonModelRepositoryBuilder(final @NotNull Class<ModelType> type) {
    this.modelType = type;
  }

  @Contract("_ -> this")
  public @NotNull GsonModelRepositoryBuilder<ModelType, Reader> folder(final @NotNull Path folderPath) {
    this.folderPath = folderPath;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull GsonModelRepositoryBuilder<ModelType, Reader> prettyPrinting(final boolean prettyPrinting) {
    this.prettyPrinting = prettyPrinting;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull GsonModelRepositoryBuilder<ModelType, Reader> modelWriter(
    final ModelCodec.@NotNull Writer<ModelType, JsonObject> writer
  ) {
    this.writer = writer;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull GsonModelRepositoryBuilder<ModelType, Reader> readerFactory(
    final @NotNull Function<JsonObject, Reader> readerFactory
  ) {
    this.readerFactory = readerFactory;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull GsonModelRepositoryBuilder<ModelType, Reader> modelReader(
    final ModelCodec.@NotNull Reader<ModelType, JsonObject, Reader> reader
  ) {
    this.reader = reader;
    return this;
  }

  @Contract("_ -> new")
  public @NotNull AsyncModelRepository<ModelType> build(final @NotNull Executor executor) {
    if (Files.notExists(this.folderPath)) {
      try {
        Files.createDirectory(this.folderPath);
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
    return new GsonModelRepository<>(
      executor,
      this.modelType,
      this.folderPath,
      this.prettyPrinting,
      this.writer,
      this.readerFactory,
      this.reader);
  }

  @Contract("_, _ -> new")
  public @NotNull CachedModelRepository<ModelType> buildCached(
    final @NotNull Executor executor,
    final @NotNull ModelRepository<ModelType> cacheModelRepository
  ) {
    return new CachedModelRepository<>(executor, cacheModelRepository, this.build(executor));
  }
}
