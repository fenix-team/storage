package ml.stargirls.storage.dist;

import ml.stargirls.storage.ModelService;
import ml.stargirls.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executor;

public class DelegatedCachedModelService<T extends Model>
		extends CachedRemoteModelService<T> {

	protected final ModelService<T> delegate;

	public DelegatedCachedModelService(
			@NotNull Executor executor,
			@NotNull ModelService<T> cacheModelService,
			@NotNull ModelService<T> delegate
	) {
		super(executor, cacheModelService);
		this.delegate = delegate;
	}

	@Override
	public List<T> findSync(@NotNull String field, @NotNull String value) {
		return delegate.findSync(field, value);
	}

	@Override
	protected void internalSave(@NotNull T model) {
		delegate.saveSync(model);
	}

	@Override
	protected boolean internalDelete(@NotNull String id) {
		return delegate.deleteSync(id);
	}

	@Override
	protected @Nullable T internalFind(@NotNull String id) {
		return delegate.findSync(id);
	}

	@Override
	protected List<T> internalFindAll() {
		return delegate.findAllSync();
	}
}
