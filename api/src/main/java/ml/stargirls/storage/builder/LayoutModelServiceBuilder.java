package ml.stargirls.storage.builder;

import ml.stargirls.storage.ModelService;
import ml.stargirls.storage.model.Model;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class LayoutModelServiceBuilder
		<T extends Model, O extends ModelServiceBuilder<T>>
		implements ModelServiceBuilder<T> {

	protected final Class<T> type;
	protected Executor executor;
	protected ModelService<T> cacheModelService;

	public LayoutModelServiceBuilder(@NotNull Class<T> type) {
		this.type = type;
	}

	@Override
	public O executor(@NotNull Executor executor) {
		this.executor = executor;
		return back();
	}

	@Override
	public O cachedService(@NotNull ModelService<T> cachedService) {
		this.cacheModelService = cachedService;
		return back();
	}

	protected void check() {
		if (executor == null) {
			executor = Executors.newSingleThreadExecutor();
		}
	}

	protected abstract O back();
}
