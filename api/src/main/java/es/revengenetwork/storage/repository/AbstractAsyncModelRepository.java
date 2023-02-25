package es.revengenetwork.storage.repository;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractAsyncModelRepository<ModelType extends Model>
  implements ModelRepository<ModelType>, AsyncModelRepository<ModelType> {

  protected final Executor executor;

  public AbstractAsyncModelRepository(@NotNull Executor executor) {
    this.executor = executor;
  }

  @Override
  public @NotNull CompletableFuture<@Nullable ModelType> find(@NotNull String id) {
    return CompletableFuture.supplyAsync(() -> findSync(id), executor);
  }

  @Override
  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> find(
    @NotNull final String field,
    @NotNull final String value,
    @NotNull final Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> findSync(field, value, factory), executor);
  }

  @Override
  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> findAll(
    @NotNull final Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> findAllSync(factory), executor);
  }

  @Override
  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> findAll(
    @NotNull final Consumer<ModelType> postLoadAction,
    @NotNull final Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> findAllSync(postLoadAction, factory), executor);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable Collection<String>> findIds() {
    return CompletableFuture.supplyAsync(this::findIdsSync, executor);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Boolean> exists(@NotNull final String id) {
    return CompletableFuture.supplyAsync(() -> existsSync(id), executor);
  }

  @Override
  public @NotNull CompletableFuture<Void> save(@NotNull ModelType model) {
    return CompletableFuture.runAsync(() -> saveSync(model), executor);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Boolean> delete(@NotNull ModelType model) {
    return CompletableFuture.supplyAsync(() -> deleteSync(model), executor);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Boolean> delete(@NotNull String id) {
    return CompletableFuture.supplyAsync(() -> deleteSync(id), executor);
  }
}
