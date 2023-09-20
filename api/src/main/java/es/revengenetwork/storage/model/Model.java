package es.revengenetwork.storage.model;

import org.jetbrains.annotations.NotNull;

/**
 * This class is the base of our models, it contains the method to get the id of the model, so it
 * can be used in the {@link es.revengenetwork.storage.repository.ModelRepository} to find, save or
 * delete the model.
 * It's important to consider that models are entities that are stored in a persistent storage (most
 * of the time), so, you should consider that the id of the model is unique and immutable.
 *
 * @since 1.0.0
 */
public interface Model {
  /**
   * Returns the id of the model as a {@link String}, this id is unique and immutable.
   *
   * @return The id of the model.
   * @since 1.0.0
   */
  @NotNull String id();
}
