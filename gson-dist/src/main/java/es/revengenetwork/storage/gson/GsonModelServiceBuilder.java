package es.revengenetwork.storage.gson;

import com.google.gson.Gson;
import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.builder.LayoutModelServiceBuilder;
import es.revengenetwork.storage.dist.DelegatedCachedModelService;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class GsonModelServiceBuilder<ModelType extends Model>
  extends LayoutModelServiceBuilder<ModelType, GsonModelServiceBuilder<ModelType>> {

  private Gson gson;
  private File folder;

  protected GsonModelServiceBuilder(@NotNull Class<ModelType> type) {
    super(type);
  }

  @Contract("_ -> this")
  public GsonModelServiceBuilder<ModelType> gson(@NotNull Gson gson) {
    this.gson = gson;
    return back();
  }

  @Contract("_ -> this")
  public GsonModelServiceBuilder<ModelType> folder(@NotNull File folder) {
    this.folder = folder;
    return back();
  }

  @Override
  protected GsonModelServiceBuilder<ModelType> back() {
    return this;
  }

  @Override
  public ModelService<ModelType> build() {
    check();

    if (!folder.exists()) {
      folder.mkdirs();
    }

    ModelService<ModelType> modelService = new GsonModelService<>(
      executor, gson,
      type, folder
    );

    if (cacheModelService == null) {
      return modelService;
    } else {
      return new DelegatedCachedModelService<>(executor, cacheModelService, modelService);
    }
  }
}
