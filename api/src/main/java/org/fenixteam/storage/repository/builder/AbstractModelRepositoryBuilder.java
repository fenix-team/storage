package org.fenixteam.storage.repository.builder;

import java.util.concurrent.Executor;
import org.fenixteam.storage.model.Model;
import org.fenixteam.storage.repository.AsyncModelRepository;
import org.fenixteam.storage.repository.CachedModelRepository;
import org.fenixteam.storage.repository.ModelRepository;
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
