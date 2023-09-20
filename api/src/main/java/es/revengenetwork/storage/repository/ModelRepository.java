package es.revengenetwork.storage.repository;

import es.revengenetwork.storage.model.Model;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is the base of our repositories, it contains the essential methods to interact with
 * the database, cache, or whatever you want to use to store your data. It's important to note that
 * this class is not a singleton, so you can create as many instances as you want.
 *
 * @param <ModelType> The {@link Model} type that this repository will handle.
 */
public interface ModelRepository<ModelType extends Model> {
  /**
   * Returns the model with the specified id, or null if it doesn't exist.
   *
   * @param id The id of the model.
   * @return The model with the specified id, or null if it doesn't exist.
   */
  @Nullable ModelType findSync(final @NotNull String id);

  @Nullable Collection<String> findIdsSync();

  default <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Function<Integer, C> factory
  ) {
    return this.findAllSync(modelType -> {
    }, factory);
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
