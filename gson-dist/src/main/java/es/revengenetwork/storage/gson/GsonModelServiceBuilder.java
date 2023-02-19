package es.revengenetwork.storage.gson;

import com.google.gson.Gson;
import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.builder.LayoutModelServiceBuilder;
import es.revengenetwork.storage.dist.DelegatedCachedModelService;
import es.revengenetwork.storage.model.Model;
import ml.stargirls.storage.util.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class GsonModelServiceBuilder<T extends Model>
  extends LayoutModelServiceBuilder<T, GsonModelServiceBuilder<T>> {

  private Gson gson;
  private File folder;

  protected GsonModelServiceBuilder(@NotNull Class<T> type) {
    super(type);
  }

  @Contract("_ -> this")
  public GsonModelServiceBuilder<T> gson(@NotNull Gson gson) {
    this.gson = gson;
    return back();
  }

  @Contract("_ -> this")
  public GsonModelServiceBuilder<T> folder(@NotNull File folder) {
    this.folder = folder;
    return back();
  }

  @Override
  protected GsonModelServiceBuilder<T> back() {
    return this;
  }

  @Override
  public ModelService<T> build() {
    check();
    Validate.notNull(gson, "gson");
    Validate.notNull(folder, "folder");

    if (!folder.exists()) {
      Validate.state(folder.mkdirs(), "Failed to create folder: " + folder.getName());
    }

    ModelService<T> modelService = new GsonModelService<>(
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
