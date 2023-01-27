package ml.stargirls.storage;

import ml.stargirls.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
public interface AsyncModelService<T extends Model> {

	@NotNull CompletableFuture<@Nullable T> find(@NotNull String id);

	@NotNull CompletableFuture<@Nullable List<T>> find(@NotNull String field, @NotNull String value);

	@NotNull CompletableFuture<@Nullable List<T>> findAll();

	@NotNull CompletableFuture<@Nullable List<T>> findAll(@NotNull Consumer<T> postLoadAction);

	@NotNull CompletableFuture<Void> save(@NotNull T model);

	@NotNull CompletableFuture<Void> delete(@NotNull T model);

	@NotNull CompletableFuture<@NotNull Boolean> delete(@NotNull String id);
}
