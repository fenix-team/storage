package es.revengenetwork.storage.repository;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ModelRepository<ModelType extends Model> {

  String ID_FIELD = "id";

  @Nullable ModelType findSync(@NotNull String id);

  <C extends Collection<ModelType>> @Nullable C findSync(
    @NotNull String field,
    @NotNull String value,
    @NotNull Function<Integer, C> factory
  );

  @Nullable Collection<String> findIdsSync();

  default <C extends Collection<ModelType>> @Nullable C findAllSync(
    @NotNull Function<Integer, C> factory
  ) {
    return findAllSync(modelType -> { }, factory);
  }

  <C extends Collection<ModelType>> @Nullable C findAllSync(
    @NotNull Consumer<ModelType> postLoadAction,
    @NotNull Function<Integer, C> factory
  );

  boolean existsSync(@NotNull String id);

  void saveSync(@NotNull ModelType model);

  default boolean deleteSync(@NotNull ModelType model) {
    return deleteSync(model.getId());
  }

  boolean deleteSync(@NotNull String id);
}
