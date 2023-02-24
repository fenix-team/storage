package es.revengenetwork.storage;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface AsyncModelRepository<ModelType extends Model> {

  @NotNull CompletableFuture<@Nullable ModelType> find(@NotNull String id);

  @NotNull CompletableFuture<@Nullable List<ModelType>> find(
    @NotNull String field,
    @NotNull String value
  );

  @NotNull CompletableFuture<@Nullable Collection<String>> findIds();

  @NotNull CompletableFuture<@Nullable List<ModelType>> findAll();

  @NotNull CompletableFuture<@Nullable List<ModelType>> findAll(@NotNull Consumer<ModelType> postLoadAction);

  @NotNull CompletableFuture<@NotNull Boolean> exists(@NotNull String id);

  @NotNull CompletableFuture<Void> save(@NotNull ModelType model);

  @NotNull CompletableFuture<@NotNull Boolean> delete(@NotNull ModelType model);

  @NotNull CompletableFuture<@NotNull Boolean> delete(@NotNull String id);
}
