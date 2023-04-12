package es.revengenetwork.storage.repository.builder;

import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.repository.AsyncModelRepository;
import es.revengenetwork.storage.repository.CachedModelRepository;
import es.revengenetwork.storage.repository.ModelRepository;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class AbstractModelRepositoryBuilder<ModelType extends Model> {
  @Contract("_ -> new")
  public abstract @NotNull AsyncModelRepository<ModelType> build(final @NotNull Executor executor);

  @Contract("_, _ -> new")
  public @NotNull CachedModelRepository<ModelType> buildCached(
    final @NotNull Executor executor,
    final @NotNull ModelRepository<ModelType> cacheModelRepository
  ) {
    return new CachedModelRepository<>(executor, cacheModelRepository, this.build(executor));
  }
}
