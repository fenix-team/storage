package es.revengenetwork.storage;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface CachedModelRepository<ModelType extends Model>
  extends ModelRepository<ModelType> {

  @Nullable ModelType getSync(@NotNull String id);

  @Nullable ModelType getOrFindSync(@NotNull String id);

  @Nullable ModelType getOrFindAndCacheSync(@NotNull String id);

  @Nullable List<ModelType> getAllSync();

  void uploadSync(@NotNull ModelType model);

  void uploadAllSync(@NotNull Consumer<ModelType> preUploadAction);

  default void uploadAllSync() {
    uploadAllSync(modelType -> { });
  }

  void saveInCacheSync(@NotNull ModelType model);

  void saveInBothSync(@NotNull ModelType model);

  boolean deleteInCacheSync(@NotNull String id);

  default boolean deleteInCacheSync(@NotNull ModelType model) {
    return deleteInCacheSync(model.getId());
  }

  boolean deleteInBothSync(@NotNull String id);

  void saveAllSync(@NotNull Consumer<ModelType> preSaveAction);

  default void saveAllSync() {
    saveAllSync(modelType -> { });
  }
}
