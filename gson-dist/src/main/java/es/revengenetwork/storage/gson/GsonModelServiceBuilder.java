package es.revengenetwork.storage.gson;

import com.google.gson.Gson;
import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.builder.LayoutModelServiceBuilder;
import es.revengenetwork.storage.dist.DelegatedCachedModelService;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GsonModelServiceBuilder<ModelType extends Model>
  extends LayoutModelServiceBuilder<ModelType, GsonModelServiceBuilder<ModelType>> {

  private Gson gson;
  private Path folderPath;

  protected GsonModelServiceBuilder(@NotNull Class<ModelType> type) {
    super(type);
  }

  @Contract("_ -> this")
  public GsonModelServiceBuilder<ModelType> gson(@NotNull Gson gson) {
    this.gson = gson;
    return back();
  }

  @Contract("_ -> this")
  public GsonModelServiceBuilder<ModelType> folder(@NotNull Path folderPath) {
    this.folderPath = folderPath;
    return back();
  }

  @Override
  protected GsonModelServiceBuilder<ModelType> back() {
    return this;
  }

  @Override
  public ModelService<ModelType> build() {
    check();

    if (Files.notExists(this.folderPath)) {
      try {
        Files.createDirectory(this.folderPath);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    ModelService<ModelType> modelService = new GsonModelService<>(executor, gson, type, folderPath);

    if (cacheModelService == null) {
      return modelService;
    } else {
      return new DelegatedCachedModelService<>(executor, cacheModelService, modelService);
    }
  }
}
