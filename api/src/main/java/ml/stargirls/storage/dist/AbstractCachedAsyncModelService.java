package ml.stargirls.storage.dist;

import ml.stargirls.storage.CachedAsyncModelService;
import ml.stargirls.storage.CachedModelService;
import ml.stargirls.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class AbstractCachedAsyncModelService<T extends Model>
		extends AbstractAsyncModelService<T>
		implements CachedModelService<T>, CachedAsyncModelService<T> {
	public AbstractCachedAsyncModelService(@NotNull Executor executor) {
		super(executor);
	}

	@Override
	public @NotNull CompletableFuture<@Nullable T> get(@NotNull String id) {
		return CompletableFuture.supplyAsync(() -> getSync(id), executor);
	}

	@Override
	public @NotNull CompletableFuture<@Nullable T> getOrFind(@NotNull String id) {
		return CompletableFuture.supplyAsync(() -> getOrFindSync(id), executor);
	}

	@Override
	public @NotNull CompletableFuture<@Nullable List<T>> getAll() {
		return CompletableFuture.supplyAsync(this::getAllSync, executor);
	}

	@Override
	public @NotNull CompletableFuture<Void> upload(@NotNull T model) {
		return CompletableFuture.runAsync(() -> uploadSync(model), executor);
	}

	@Override
	public @NotNull CompletableFuture<Void> uploadAll() {
		return uploadAll(t -> { });
	}

	@Override
	public @NotNull CompletableFuture<Void> uploadAll(@NotNull Consumer<T> preUploadAction) {
		return CompletableFuture.runAsync(() -> uploadAllSync(preUploadAction), executor);
	}

	@Override
	public @NotNull CompletableFuture<Void> saveInCache(@NotNull final T model) {
		return CompletableFuture.runAsync(() -> saveInCacheSync(model), executor);
	}

	@Override
	public @NotNull CompletableFuture<Void> deleteInCache(@NotNull final String id) {
		return CompletableFuture.runAsync(() -> deleteInCacheSync(id), executor);
	}

	@Override
	public @NotNull CompletableFuture<Void> saveAll() {
		return saveAll(t -> { });
	}

	@Override
	public @NotNull CompletableFuture<Void> saveAll(@NotNull Consumer<T> preSaveAction) {
		return CompletableFuture.runAsync(() -> saveAllSync(preSaveAction), executor);
	}
}
