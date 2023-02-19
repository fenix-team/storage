package es.revengenetwork.storage;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface CachedAsyncModelService<T extends Model> {

  @NotNull CompletableFuture<@Nullable T> get(@NotNull String id);

  @NotNull CompletableFuture<@Nullable T> getOrFind(@NotNull String id);

  @NotNull CompletableFuture<@Nullable List<T>> getAll();

  @NotNull CompletableFuture<Void> upload(@NotNull T model);

  @NotNull CompletableFuture<Void> uploadAll();

  @NotNull CompletableFuture<Void> uploadAll(@NotNull Consumer<T> preUploadAction);

  @NotNull CompletableFuture<Void> saveInCache(@NotNull T model);

  @NotNull CompletableFuture<Void> deleteInCache(@NotNull String id);

  default @NotNull CompletableFuture<Void> deleteInCache(@NotNull T model) {
    return deleteInCache(model.getId());
  }

  @NotNull CompletableFuture<Void> saveAll();

  @NotNull CompletableFuture<Void> saveAll(@NotNull Consumer<T> preSaveAction);
}
