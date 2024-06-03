package org.fenixteam.storage.repository;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import org.fenixteam.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAsyncModelRepository<ModelType extends Model> implements AsyncModelRepository<ModelType> {
  protected final Executor executor;

  public AbstractAsyncModelRepository(final @NotNull Executor executor) {
    this.executor = executor;
  }

  @Override
  public @NotNull CompletableFuture<@Nullable ModelType> find(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.findSync(id), this.executor);
  }

  @Override
  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> find(
    final @NotNull String field,
    final @NotNull String value,
    final @NotNull Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> this.findSync(field, value, factory), this.executor);
  }

  @Override
  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> findAll(
    final @NotNull Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> this.findAllSync(factory), this.executor);
  }

  @Override
  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> findAll(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> this.findAllSync(postLoadAction, factory), this.executor);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable Collection<String>> findIds() {
    return CompletableFuture.supplyAsync(this::findIdsSync, this.executor);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Boolean> exists(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.existsSync(id), this.executor);
  }

  @Override
  public @NotNull CompletableFuture<ModelType> save(final @NotNull ModelType model) {
    return CompletableFuture.supplyAsync(() -> this.saveSync(model), this.executor);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Boolean> delete(final @NotNull ModelType model) {
    return CompletableFuture.supplyAsync(() -> this.deleteSync(model), this.executor);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Boolean> delete(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.deleteSync(id), this.executor);
  }
}
