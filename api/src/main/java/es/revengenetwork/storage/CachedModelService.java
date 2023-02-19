package es.revengenetwork.storage;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface CachedModelService<T extends Model>
  extends ModelService<T> {

  @Nullable T getSync(@NotNull String id);

  @Nullable T getOrFindSync(@NotNull String id);

  @Contract(pure = true)
  @Nullable List<T> getAllSync();

  /**
   * Uploads the model to the server
   *
   * @param model
   *   The model to be uploaded.
   */
  @Contract(pure = true)
  void uploadSync(@NotNull T model);

  /**
   * Upload all the files in the current directory to the remote server
   *
   * @param preUploadAction
   *   a function that takes a single parameter, which is the file to be uploaded.
   */
  @Contract(pure = true)
  void uploadAllSync(@NotNull Consumer<T> preUploadAction);

  @Contract(pure = true)
  default void uploadAllSync() {
    uploadAllSync(t -> { });
  }

  void saveInCacheSync(@NotNull T model);

  boolean deleteInCacheSync(@NotNull String id);

  default boolean deleteInCacheSync(@NotNull T model) {
    return deleteInCacheSync(model.getId());
  }

  @Contract(pure = true)
  void saveAllSync(@NotNull Consumer<T> preSaveAction);

  @Contract(pure = true)
  default void saveAllSync() {
    saveAllSync(t -> { });
  }
}
