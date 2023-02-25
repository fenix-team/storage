package es.revengenetwork.storage.repository;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface AsyncModelRepository<ModelType extends Model> {

  @NotNull CompletableFuture<@Nullable ModelType> find(@NotNull String id);

  <C extends Collection<ModelType>> @NotNull CompletableFuture<@Nullable C> find(
    @NotNull String field,
    @NotNull String value,
    @NotNull Function<Integer, C> factory
  );

  @NotNull CompletableFuture<@Nullable Collection<String>> findIds();

  <C extends Collection<ModelType>> @NotNull CompletableFuture<@Nullable C> findAll(
    @NotNull Function<Integer, C> factory
  );

  <C extends Collection<ModelType>> @NotNull CompletableFuture<@Nullable C> findAll(
    @NotNull Consumer<ModelType> postLoadAction,
    @NotNull Function<Integer, C> factory
  );

  @NotNull CompletableFuture<@NotNull Boolean> exists(@NotNull String id);

  @NotNull CompletableFuture<Void> save(@NotNull ModelType model);

  @NotNull CompletableFuture<@NotNull Boolean> delete(@NotNull ModelType model);

  @NotNull CompletableFuture<@NotNull Boolean> delete(@NotNull String id);
}
