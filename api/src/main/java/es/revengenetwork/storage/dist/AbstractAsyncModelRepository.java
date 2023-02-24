package es.revengenetwork.storage.dist;

import es.revengenetwork.storage.AsyncModelRepository;
import es.revengenetwork.storage.ModelRepository;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

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
  public @NotNull CompletableFuture<@Nullable List<ModelType>> find(
    @NotNull String field,
    @NotNull String value
  ) {
    return CompletableFuture.supplyAsync(() -> findSync(field, value), executor);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable Collection<String>> findIds() {
    return CompletableFuture.supplyAsync(this::findIdsSync, executor);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable List<ModelType>> findAll() {
    return CompletableFuture.supplyAsync(this::findAllSync, executor);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable List<ModelType>> findAll(@NotNull Consumer<ModelType> postLoadAction) {
    return CompletableFuture.supplyAsync(() -> findAllSync(postLoadAction), executor);
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
