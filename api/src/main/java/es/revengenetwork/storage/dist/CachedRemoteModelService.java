package es.revengenetwork.storage.dist;

import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class CachedRemoteModelService<ModelType extends Model>
  extends AbstractCachedAsyncModelService<ModelType> {

  protected final ModelService<ModelType> cacheModelService;

  public CachedRemoteModelService(
    @NotNull Executor executor,
    @NotNull ModelService<ModelType> cacheModelService
  ) {
    super(executor);
    this.cacheModelService = cacheModelService;
  }

  @Override
  public @Nullable ModelType findSync(@NotNull String id) {
    ModelType model = internalFind(id);

    if (model != null) {
      // add to cache
      cacheModelService.saveSync(model);
    }

    return model;
  }

  @Override
  public @Nullable ModelType getSync(@NotNull String id) {
    return cacheModelService.findSync(id);
  }

  @Override
  public @Nullable ModelType getOrFindSync(@NotNull String id) {
    ModelType model = getSync(id);

    if (model != null) {
      return model;
    }

    return findSync(id);
  }

  @Override
  public List<ModelType> getAllSync() {
    return cacheModelService.findAllSync();
  }

  @Override
  public List<ModelType> findAllSync(@NotNull Consumer<ModelType> postLoadAction) {
    List<ModelType> loadedModels = internalFindAll();

    for (ModelType model : loadedModels) {
      postLoadAction.accept(model);
      cacheModelService.saveSync(model);
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
  public void saveSync(@NotNull ModelType model) {
    saveInCacheSync(model);
    internalSave(model);
  }

  @Override
  public void uploadSync(@NotNull ModelType model) {
    deleteInCacheSync(model.getId());
    internalSave(model);
  }

  @Override
  public void uploadAllSync(@NotNull Consumer<ModelType> preUploadAction) {
    List<ModelType> models = cacheModelService.findAllSync();

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
    return deleteInCacheSync(id) && internalDelete(id);
  }

  @Override
  public void saveInCacheSync(@NotNull ModelType model) {
    cacheModelService.saveSync(model);
  }

  @Override
  public boolean deleteInCacheSync(@NotNull String id) {
    return cacheModelService.deleteSync(id);
  }

  protected abstract void internalSave(ModelType model);

  protected abstract boolean internalDelete(String id);

  protected abstract @Nullable ModelType internalFind(String id);

  protected abstract List<ModelType> internalFindAll();
}
