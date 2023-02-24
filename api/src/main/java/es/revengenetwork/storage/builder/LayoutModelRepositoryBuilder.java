package es.revengenetwork.storage.builder;

import es.revengenetwork.storage.ModelRepository;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class LayoutModelRepositoryBuilder
  <ModelType extends Model, Builder extends ModelRepositoryBuilder<ModelType>>
  implements ModelRepositoryBuilder<ModelType> {

  protected final Class<ModelType> type;
  protected Executor executor;
  protected ModelRepository<ModelType> cacheModelRepository;

  public LayoutModelRepositoryBuilder(@NotNull Class<ModelType> type) {
    this.type = type;
  }

  @Override
  public Builder executor(@NotNull Executor executor) {
    this.executor = executor;
    return back();
  }

  @Override
  public Builder cachedService(@NotNull ModelRepository<ModelType> cachedService) {
    this.cacheModelRepository = cachedService;
    return back();
  }

  protected void check() {
    if (executor == null) {
      executor = Executors.newSingleThreadExecutor();
    }
  }

  protected abstract Builder back();
}
