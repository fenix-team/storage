package es.revengenetwork.storage.dist;

import es.revengenetwork.storage.CachedAsyncModelService;
import es.revengenetwork.storage.CachedModelService;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class AbstractCachedAsyncModelService<ModelType extends Model>
  extends AbstractAsyncModelService<ModelType>
  implements CachedModelService<ModelType>, CachedAsyncModelService<ModelType> {

  public AbstractCachedAsyncModelService(@NotNull Executor executor) {
    super(executor);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable ModelType> get(@NotNull String id) {
    return CompletableFuture.supplyAsync(() -> getSync(id), executor);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable ModelType> getOrFind(@NotNull String id) {
    return CompletableFuture.supplyAsync(() -> getOrFindSync(id), executor);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable List<ModelType>> getAll() {
    return CompletableFuture.supplyAsync(this::getAllSync, executor);
  }

  @Override
  public @NotNull CompletableFuture<Void> upload(@NotNull ModelType model) {
    return CompletableFuture.runAsync(() -> uploadSync(model), executor);
  }

  @Override
  public @NotNull CompletableFuture<Void> uploadAll() {
    return uploadAll(modelType -> { });
  }

  @Override
  public @NotNull CompletableFuture<Void> uploadAll(@NotNull Consumer<ModelType> preUploadAction) {
    return CompletableFuture.runAsync(() -> uploadAllSync(preUploadAction), executor);
  }

  @Override
  public @NotNull CompletableFuture<Void> saveInCache(@NotNull final ModelType model) {
    return CompletableFuture.runAsync(() -> saveInCacheSync(model), executor);
  }

  @Override
  public @NotNull CompletableFuture<Void> deleteInCache(@NotNull final String id) {
    return CompletableFuture.runAsync(() -> deleteInCacheSync(id), executor);
  }

  @Override
  public @NotNull CompletableFuture<Void> saveAll() {
    return saveAll(modelType -> { });
  }

  @Override
  public @NotNull CompletableFuture<Void> saveAll(@NotNull Consumer<ModelType> preSaveAction) {
    return CompletableFuture.runAsync(() -> saveAllSync(preSaveAction), executor);
  }
}
