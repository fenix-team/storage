package es.revengenetwork.storage;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface CachedAsyncModelRepository<ModelType extends Model> {

  @NotNull CompletableFuture<@Nullable ModelType> get(@NotNull String id);

  @NotNull CompletableFuture<@Nullable ModelType> getOrFind(@NotNull String id);

  @NotNull CompletableFuture<@Nullable ModelType> getOrFindAndCache(@NotNull String id);

  @NotNull CompletableFuture<@Nullable List<ModelType>> getAll();

  @NotNull CompletableFuture<Void> upload(@NotNull ModelType model);

  @NotNull CompletableFuture<Void> uploadAll();

  @NotNull CompletableFuture<Void> uploadAll(@NotNull Consumer<ModelType> preUploadAction);

  @NotNull CompletableFuture<Void> saveInCache(@NotNull ModelType model);

  @NotNull CompletableFuture<Void> saveInBoth(@NotNull ModelType model);

  @NotNull CompletableFuture<Void> deleteInCache(@NotNull String id);

  default @NotNull CompletableFuture<Void> deleteInCache(@NotNull ModelType model) {
    return deleteInCache(model.getId());
  }

  @NotNull CompletableFuture<Void> deleteInBoth(@NotNull String id);

  @NotNull CompletableFuture<Void> saveAll();

  @NotNull CompletableFuture<Void> saveAll(@NotNull Consumer<ModelType> preSaveAction);
}
