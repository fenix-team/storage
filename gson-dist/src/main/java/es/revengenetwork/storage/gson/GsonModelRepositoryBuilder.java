package es.revengenetwork.storage.gson;

import com.google.gson.Gson;
import es.revengenetwork.storage.ModelRepository;
import es.revengenetwork.storage.builder.LayoutModelRepositoryBuilder;
import es.revengenetwork.storage.dist.DelegatedCachedModelRepository;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GsonModelRepositoryBuilder<ModelType extends Model>
  extends LayoutModelRepositoryBuilder<ModelType, GsonModelRepositoryBuilder<ModelType>> {

  private Gson gson;
  private Path folderPath;

  protected GsonModelRepositoryBuilder(@NotNull Class<ModelType> type) {
    super(type);
  }

  @Contract("_ -> this")
  public GsonModelRepositoryBuilder<ModelType> gson(@NotNull Gson gson) {
    this.gson = gson;
    return back();
  }

  @Contract("_ -> this")
  public GsonModelRepositoryBuilder<ModelType> folder(@NotNull Path folderPath) {
    this.folderPath = folderPath;
    return back();
  }

  @Override
  protected GsonModelRepositoryBuilder<ModelType> back() {
    return this;
  }

  @Override
  public ModelRepository<ModelType> build() {
    check();

    if (Files.notExists(this.folderPath)) {
      try {
        Files.createDirectory(this.folderPath);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    ModelRepository<ModelType> modelRepository = new GsonModelRepository<>(executor, gson, type, folderPath);

    if (cacheModelRepository == null) {
      return modelRepository;
    } else {
      return new DelegatedCachedModelRepository<>(executor, cacheModelRepository, modelRepository);
    }
  }
}
