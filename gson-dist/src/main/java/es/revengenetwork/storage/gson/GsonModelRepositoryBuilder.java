package es.revengenetwork.storage.gson;

import com.google.gson.Gson;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.repository.AsyncModelRepository;
import es.revengenetwork.storage.repository.CachedModelRepository;
import es.revengenetwork.storage.repository.ModelRepository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;

@SuppressWarnings("unused")
public final class GsonModelRepositoryBuilder<ModelType extends Model> {
  private final Class<ModelType> type;
  private Gson gson;
  private Path folderPath;

  GsonModelRepositoryBuilder(final @NotNull Class<ModelType> type) {
    this.type = type;
  }

  @Contract("_ -> this")
  public @NotNull GsonModelRepositoryBuilder<ModelType> gson(final @NotNull Gson gson) {
    this.gson = gson;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull GsonModelRepositoryBuilder<ModelType> folder(final @NotNull Path folderPath) {
    this.folderPath = folderPath;
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
    return new GsonModelRepository<>(executor, this.gson, this.type, this.folderPath);
  }

  @Contract("_, _ -> new")
  public @NotNull CachedModelRepository<ModelType> buildCached(
    final @NotNull Executor executor,
    final @NotNull ModelRepository<ModelType> cacheModelRepository
  ) {
    return new CachedModelRepository<>(executor, cacheModelRepository, this.build(executor));
  }
}
