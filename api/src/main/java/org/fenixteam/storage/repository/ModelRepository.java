/*
 * This file is part of storage, licensed under the MIT License
 *
 * Copyright (c) 2023 FenixTeam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.fenixteam.storage.repository;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import org.fenixteam.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is the base of our repositories, it contains the essential methods to interact with
 * the database, cache, or whatever you want to use to store your data.
 *
 * @param <ModelType> The {@link Model} type that this repository will handle.
 * @since 1.0.0
 */
public interface ModelRepository<ModelType extends Model> {
  /**
   * Returns the model with the specified id, or null if it doesn't exist.
   *
   * @param id The id of the {@link ModelType}.
   * @return The {@link ModelType} with the specified id, or null if it doesn't exist.
   * @since 1.0.0
   */
  @Nullable ModelType findSync(final @NotNull String id);

  /**
   * Returns a {@link Collection} of all the ids of the {@link ModelType}s in the repository. This method is equivalent
   * to calling {@link #findIdsSync(Function)} with a {@link Collection} factory that creates a
   * {@link Collection} of the same type as the one passed as parameter except that this method
   * doesn't make any assumptions about the type of the {@link Collection} to return.
   *
   * @return A {@link Collection} containing all the ids of the {@link ModelType}s in the repository.
   * @see #findIdsSync(Function)
   * @since 1.0.0
   */
  @Nullable Collection<String> findIdsSync();

  /**
   * Returns a {@link Collection} of all the ids of the {@link ModelType}s in the repository. This method
   * will create a {@link Collection} of the specified type and return it, for doing this it can
   * iterate over the {@link Model}s in the repository, so it's important to note that this method
   * can be slow if the repository contains a lot of models. It's also important to note that the
   * {@link Collection} returned by this method will be {@code null} if the repository is empty.
   *
   * @param factory The factory to create the {@link Collection} to return.
   * @param <C>     The type of the {@link Collection} to return.
   * @return A {@link Collection} containing all the ids of the {@link ModelType}s in the repository.
   * @since 1.0.0
   */
  <C extends Collection<String>> @Nullable C findIdsSync(final @NotNull Function<Integer, C> factory);

  /**
   * Finds all the models in the repository and returns them in the specified {@link Collection}.
   *
   * @param factory The factory to create the {@link Collection} to return.
   * @param <C>     The type of the {@link Collection} to return.
   * @return A {@link Collection} containing all the models in the repository.
   * @since 1.0.0
   */
  default <C extends Collection<ModelType>> @Nullable C findAllSync(final @NotNull Function<Integer, C> factory) {
    return this.findAllSync(modelType -> {}, factory);
  }

  /**
   * Finds all the models in the repository and returns them in the specified {@link Collection}, it also executes
   * the specified action for each model after it's loaded so you mustn't iterate over the returned {@link Collection}.
   *
   * @param postLoadAction The action to execute for each model after it's loaded.
   * @param factory        The factory to create the {@link Collection} to return.
   * @param <C>            The type of the {@link Collection} to return.
   * @return A {@link Collection} containing all the models in the repository.
   * @since 1.0.0
   */
  <C extends Collection<ModelType>> @Nullable C findAllSync(final @NotNull Consumer<ModelType> postLoadAction, final @NotNull Function<Integer, C> factory);

  /**
   * Iterates over all the {@link ModelType}s in the repository and executes the specified action for each one.
   *
   * @param action The action to execute for each model.
   * @since 1.0.0
   */
  void forEachSync(final @NotNull Consumer<ModelType> action);

  /**
   * Returns true if the model with the specified id exists, false otherwise.
   *
   * @param id The id of the model.
   * @return True if the model with the specified id exists, false otherwise.
   * @since 1.0.0
   */
  boolean existsSync(final @NotNull String id);

  /**
   * Saves the specified {@link Model} in the repository.
   *
   * @param model The {@link Model} to save.
   * @return The saved {@link Model}.
   * @since 1.0.0
   */
  @Contract("_ -> param1")
  @NotNull ModelType saveSync(final @NotNull ModelType model);

  /**
   * Deletes the specified {@link Model} from the repository and returns true if it was deleted
   * successfully, false otherwise.
   *
   * @param id The id of the {@link Model} to delete.
   * @return True if the {@link Model} was deleted successfully, false otherwise.
   * @since 1.0.0
   */
  boolean deleteSync(final @NotNull String id);

  /**
   * Deletes the specified {@link Model} from the repository and returns it.
   *
   * @param id The id of the {@link Model} to delete.
   * @return The deleted {@link Model}, or null if it doesn't exist.
   * @since 1.0.0
   */
  @Nullable ModelType deleteAndRetrieveSync(final @NotNull String id);

  /**
   * Deletes all the {@link Model}s from the repository.
   *
   * @since 1.0.0
   */
  void deleteAll();
}
