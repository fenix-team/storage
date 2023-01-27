package ml.stargirls.storage.dist;

import ml.stargirls.storage.ModelService;
import ml.stargirls.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class CachedRemoteModelService<T extends Model>
		extends AbstractCachedAsyncModelService<T> {

	protected final ModelService<T> cacheModelService;

	public CachedRemoteModelService(@NotNull Executor executor, @NotNull ModelService<T> cacheModelService) {
		super(executor);
		this.cacheModelService = cacheModelService;
	}

	@Override
	public @Nullable T findSync(@NotNull String id) {
		T model = internalFind(id);

		if (model != null) {
			// add to cache
			cacheModelService.saveSync(model);
		}

		return model;
	}

	@Override
	public @Nullable T getSync(@NotNull String id) {
		return cacheModelService.findSync(id);
	}

	@Override
	public @Nullable T getOrFindSync(@NotNull String id) {
		T model = getSync(id);

		if (model != null) {
			return model;
		}

		return findSync(id);
	}

	@Override
	public List<T> getAllSync() {
		return cacheModelService.findAllSync();
	}

	@Override
	public List<T> findAllSync(@NotNull Consumer<T> postLoadAction) {
		List<T> loadedModels = internalFindAll();

		for (T model : loadedModels) {
			postLoadAction.accept(model);
			cacheModelService.saveSync(model);
		}

		return loadedModels;
	}

	@Override
	public void saveAllSync(@NotNull Consumer<T> preSaveAction) {
		List<T> models = getAllSync();

		if (models == null) {
			return;
		}

		for (T model : models) {
			preSaveAction.accept(model);
			internalSave(model);
		}
	}

	@Override
	public void saveSync(@NotNull T model) {
		saveInCacheSync(model);
		internalSave(model);
	}

	@Override
	public void uploadSync(@NotNull T model) {
		deleteInCacheSync(model.getId());
		internalSave(model);
	}

	@Override
	public void uploadAllSync(@NotNull Consumer<T> preUploadAction) {
		List<T> models = cacheModelService.findAllSync();

		if (models == null) {
			return;
		}

		for (T model : models) {
			preUploadAction.accept(model);
			uploadSync(model);
		}
	}

	@Override
	public boolean deleteSync(@NotNull String id) {
		return deleteInCacheSync(id) && internalDelete(id);
	}

	@Override
	public void saveInCacheSync(@NotNull T model) {
		cacheModelService.saveSync(model);
	}

	@Override
	public boolean deleteInCacheSync(@NotNull String id) {
		return cacheModelService.deleteSync(id);
	}

	protected abstract void internalSave(T model);

	protected abstract boolean internalDelete(String id);

	protected abstract @Nullable T internalFind(String id);

	protected abstract List<T> internalFindAll();
}
