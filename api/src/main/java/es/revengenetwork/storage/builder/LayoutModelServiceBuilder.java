package es.revengenetwork.storage.builder;

import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class LayoutModelServiceBuilder
  <ModelType extends Model, Builder extends ModelServiceBuilder<ModelType>>
  implements ModelServiceBuilder<ModelType> {

  protected final Class<ModelType> type;
  protected Executor executor;
  protected ModelService<ModelType> cacheModelService;

  public LayoutModelServiceBuilder(@NotNull Class<ModelType> type) {
    this.type = type;
  }

  @Override
  public Builder executor(@NotNull Executor executor) {
    this.executor = executor;
    return back();
  }

  @Override
  public Builder cachedService(@NotNull ModelService<ModelType> cachedService) {
    this.cacheModelService = cachedService;
    return back();
  }

  protected void check() {
    if (executor == null) {
      executor = Executors.newSingleThreadExecutor();
    }
  }

  protected abstract Builder back();
}
