package es.revengenetwork.storage;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface ModelRepository<ModelType extends Model> {

  String ID_FIELD = "id";

  @Nullable ModelType findSync(@NotNull String id);

  @Nullable List<ModelType> findSync(@NotNull String field, @NotNull String value);

  @Nullable Collection<String> findIdsSync();

  default @Nullable List<ModelType> findAllSync() {
    return findAllSync(modelType -> { });
  }

  @Nullable List<ModelType> findAllSync(@NotNull Consumer<ModelType> postLoadAction);

  boolean existsSync(@NotNull String id);

  void saveSync(@NotNull ModelType model);

  default boolean deleteSync(@NotNull ModelType model) {
    return deleteSync(model.getId());
  }

  boolean deleteSync(@NotNull String id);
}
