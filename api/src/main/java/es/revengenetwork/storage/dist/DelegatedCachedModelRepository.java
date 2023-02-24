package es.revengenetwork.storage.dist;

import es.revengenetwork.storage.ModelRepository;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

public class DelegatedCachedModelRepository<ModelType extends Model>
  extends CachedRemoteModelRepository<ModelType> {

  protected final ModelRepository<ModelType> delegate;

  public DelegatedCachedModelRepository(
    @NotNull Executor executor,
    @NotNull ModelRepository<ModelType> cacheModelRepository,
    @NotNull ModelRepository<ModelType> delegate
  ) {
    super(executor, cacheModelRepository);
    this.delegate = delegate;
  }

  @Override
  public List<ModelType> findSync(@NotNull String field, @NotNull String value) {
    return delegate.findSync(field, value);
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    return delegate.findIdsSync();
  }

  @Override
  public boolean existsSync(@NotNull final String id) {
    return delegate.existsSync(id);
  }

  @Override
  protected void internalSave(@NotNull ModelType model) {
    delegate.saveSync(model);
  }

  @Override
  protected boolean internalDelete(@NotNull String id) {
    return delegate.deleteSync(id);
  }

  @Override
  protected @Nullable ModelType internalFind(@NotNull String id) {
    return delegate.findSync(id);
  }

  @Override
  protected List<ModelType> internalFindAll() {
    return delegate.findAllSync();
  }
}
