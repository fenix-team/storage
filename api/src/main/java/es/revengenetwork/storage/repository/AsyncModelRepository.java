package es.revengenetwork.storage.repository;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface AsyncModelRepository<ModelType extends Model>
  extends ModelRepository<ModelType> {

  @NotNull CompletableFuture<@Nullable ModelType> find(final @NotNull String id);

  <C extends Collection<ModelType>> @NotNull CompletableFuture<@Nullable C> find(
    final @NotNull String field,
    final @NotNull String value,
    final @NotNull Function<Integer, C> factory
  );

  @NotNull CompletableFuture<@Nullable Collection<String>> findIds();

  <C extends Collection<ModelType>> @NotNull CompletableFuture<@Nullable C> findAll(
    final @NotNull Function<Integer, C> factory
  );

  <C extends Collection<ModelType>> @NotNull CompletableFuture<@Nullable C> findAll(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  );

  @NotNull CompletableFuture<@NotNull Boolean> exists(final @NotNull String id);

  @NotNull CompletableFuture<Void> save(final @NotNull ModelType model);

  @NotNull CompletableFuture<@NotNull Boolean> delete(final @NotNull ModelType model);

  @NotNull CompletableFuture<@NotNull Boolean> delete(final @NotNull String id);
}
