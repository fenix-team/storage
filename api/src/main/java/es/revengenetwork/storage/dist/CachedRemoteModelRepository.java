package es.revengenetwork.storage.dist;

import es.revengenetwork.storage.ModelRepository;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class CachedRemoteModelRepository<ModelType extends Model>
  extends AbstractCachedAsyncModelRepository<ModelType> {

  protected final ModelRepository<ModelType> cacheModelRepository;

  public CachedRemoteModelRepository(
    @NotNull Executor executor,
    @NotNull ModelRepository<ModelType> cacheModelRepository
  ) {
    super(executor);
    this.cacheModelRepository = cacheModelRepository;
  }

  @Override
  public @Nullable ModelType findSync(@NotNull String id) {
    return this.internalFind(id);
  }

  @Override
  public @Nullable ModelType getSync(@NotNull String id) {
    return this.cacheModelRepository.findSync(id);
  }

  @Override
  public @Nullable ModelType getOrFindSync(@NotNull String id) {
    ModelType model = this.getSync(id);

    if (model != null) {
      return model;
    }

    return this.findSync(id);
  }

  @Override
  public @Nullable ModelType getOrFindAndCacheSync(@NotNull final String id) {
    ModelType model = this.getSync(id);

    if (model != null) {
      return model;
    }

    model = this.findSync(id);

    if (model == null) {
      return null;
    }

    this.cacheModelRepository.saveSync(model);
    return model;
  }

  @Override
  public List<ModelType> getAllSync() {
    return cacheModelRepository.findAllSync();
  }

  @Override
  public List<ModelType> findAllSync(@NotNull Consumer<ModelType> postLoadAction) {
    List<ModelType> loadedModels = internalFindAll();

    for (ModelType model : loadedModels) {
      postLoadAction.accept(model);
      cacheModelRepository.saveSync(model);
    }

    return loadedModels;
  }

  @Override
  public void saveAllSync(@NotNull Consumer<ModelType> preSaveAction) {
    List<ModelType> models = getAllSync();

    if (models == null) {
      return;
    }

    for (ModelType model : models) {
      preSaveAction.accept(model);
      internalSave(model);
    }
  }

  @Override
  public void uploadSync(@NotNull ModelType model) {
    deleteInCacheSync(model.getId());
    internalSave(model);
  }

  @Override
  public void uploadAllSync(@NotNull Consumer<ModelType> preUploadAction) {
    List<ModelType> models = cacheModelRepository.findAllSync();

    if (models == null) {
      return;
    }

    for (ModelType model : models) {
      preUploadAction.accept(model);
      uploadSync(model);
    }
  }

  @Override
  public boolean deleteSync(@NotNull String id) {
    return internalDelete(id);
  }

  @Override
  public boolean deleteInBothSync(@NotNull final String id) {
    return deleteSync(id) && deleteInCacheSync(id);
  }

  @Override
  public boolean deleteInCacheSync(@NotNull String id) {
    return cacheModelRepository.deleteSync(id);
  }

  @Override
  public void saveInCacheSync(@NotNull ModelType model) {
    cacheModelRepository.saveSync(model);
  }

  @Override
  public void saveInBothSync(@NotNull final ModelType model) {
    saveSync(model);
    saveInCacheSync(model);
  }

  @Override
  public void saveSync(@NotNull ModelType model) {
    internalSave(model);
  }

  protected abstract void internalSave(ModelType model);

  protected abstract boolean internalDelete(String id);

  protected abstract @Nullable ModelType internalFind(String id);

  protected abstract List<ModelType> internalFindAll();
}
