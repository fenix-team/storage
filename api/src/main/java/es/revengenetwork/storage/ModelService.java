package es.revengenetwork.storage;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface ModelService<T extends Model> {

  String ID_FIELD = "id";

  /**
   * Find a sync by id
   *
   * @param id
   *   The id of the object to find.
   *
   * @return The method returns a nullable object of type T.
   */
  @Nullable T findSync(@NotNull String id);

  @Nullable List<T> findSync(@NotNull String field, @NotNull String value);

  /**
   * Return a list of all the elements in the collection
   *
   * @return Nothing
   */
  default @Nullable List<T> findAllSync() {
    return findAllSync(t -> { });
  }

  @Nullable List<T> findAllSync(@NotNull Consumer<T> postLoadAction);

  /**
   * Save the model to the database and cache if it's not already there.
   *
   * @param model
   *   The model that will be saved.
   */
  @Contract(pure = true)
  void saveSync(@NotNull T model);

  /**
   * DeleteSync deletes the model from the database
   *
   * @param model
   *   The model to be deleted.
   */
  @Contract(pure = true)
  default void deleteSync(@NotNull T model) {
    deleteSync(model.getId());
  }

  /**
   * DeleteSync deletes the object with the given id
   *
   * @param id
   *   The id of the sync to delete.
   *
   * @return If to delete was successful and the object was deleted.
   */
  boolean deleteSync(@NotNull String id);
}
