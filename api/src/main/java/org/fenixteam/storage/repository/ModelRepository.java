package org.fenixteam.storage.repository;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import org.fenixteam.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ModelRepository<ModelType extends Model> {
  String ID_FIELD = "id";

  @Nullable ModelType findSync(final @NotNull String id);

  <C extends Collection<ModelType>> @Nullable C findSync(
    final @NotNull String field,
    final @NotNull String value,
    final @NotNull Function<Integer, C> factory
  );

  @Nullable Collection<String> findIdsSync();

  default <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Function<Integer, C> factory
  ) {
    return this.findAllSync(modelType -> { }, factory);
  }

  <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  );

  boolean existsSync(final @NotNull String id);

  @Contract("_ -> param1")
  @NotNull ModelType saveSync(final @NotNull ModelType model);

  default boolean deleteSync(final @NotNull ModelType model) {
    return this.deleteSync(model.id());
  }

  boolean deleteSync(final @NotNull String id);
}
