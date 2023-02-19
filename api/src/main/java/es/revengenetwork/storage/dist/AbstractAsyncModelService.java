package es.revengenetwork.storage.dist;

import es.revengenetwork.storage.AsyncModelService;
import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class AbstractAsyncModelService<T extends Model>
  implements ModelService<T>, AsyncModelService<T> {

  protected final Executor executor;

  public AbstractAsyncModelService(@NotNull Executor executor) {
    this.executor = executor;
  }

  @Override
  public @NotNull CompletableFuture<@Nullable T> find(@NotNull String id) {
    return CompletableFuture.supplyAsync(() -> findSync(id), executor);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable List<T>> find(
    @NotNull String field,
    @NotNull String value
  ) {
    return CompletableFuture.supplyAsync(() -> findSync(field, value), executor);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable List<T>> findAll() {
    return CompletableFuture.supplyAsync(this::findAllSync, executor);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable List<T>> findAll(@NotNull Consumer<T> postLoadAction) {
    return CompletableFuture.supplyAsync(() -> findAllSync(postLoadAction), executor);
  }

  @Override
  public @NotNull CompletableFuture<Void> save(@NotNull T model) {
    return CompletableFuture.runAsync(() -> saveSync(model), executor);
  }

  @Override
  public @NotNull CompletableFuture<Void> delete(@NotNull T model) {
    return CompletableFuture.runAsync(() -> deleteSync(model), executor);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Boolean> delete(@NotNull String id) {
    return CompletableFuture.supplyAsync(() -> deleteSync(id), executor);
  }
}
