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
import java.util.function.IntFunction;
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
   * Returns the {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   *
   * @param id The id of the {@link ModelType}.
   * @return The {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   * @since 1.0.0
   */
  @Nullable ModelType findSync(final @NotNull String id);

  /**
   * Returns a {@link Collection} of all the ids of the {@link ModelType}s in the repository. This method
   * doesn't care about the type of the {@link Collection} to return. If you want to specify the type
   * of the {@link Collection} to return, use {@link #findIdsSync(IntFunction)}.
   *
   * @return A {@link Collection} containing all the ids of the {@link ModelType}s in the repository or {@code null} if the repository is empty.
   * @see #findIdsSync(IntFunction)
   * @since 1.0.0
   */
  @Nullable Collection<@NotNull String> findIdsSync();

  /**
   * Returns a {@link Collection} of all the ids of the {@link ModelType}s in the repository. This method
   * will create a {@link Collection} of the specified type and return it, for doing this it can
   * iterate over the {@link Model}s in the repository, so it's important to note that this method
   * can be slow if the repository contains a lot of models. It's also important to note that the
   * {@link Collection} returned by this method will be {@code null} if the repository is empty.
   *
   * @param factory The factory to create the {@link Collection} to return.
   * @param <C>     The type of the {@link Collection} to return.
   * @return A {@link Collection} containing all the ids of the {@link ModelType}s in the repository or {@code null} if the repository is empty.
   * @since 1.0.0
   */
  <C extends Collection<@NotNull String>> @Nullable C findIdsSync(final @NotNull IntFunction<@NotNull C> factory);

  /**
   * Finds all the {@link ModelType} in the repository and returns them in the specified {@link Collection}.
   *
   * @param factory The factory to create the {@link Collection} to return.
   * @param <C>     The type of the {@link Collection} to return.
   * @return A {@link Collection} containing all the {@link ModelType}s in the repository or {@code null} if the repository is empty.
   * @since 1.0.0
   */
  default <C extends Collection<@NotNull ModelType>> @Nullable C findAllSync(final @NotNull IntFunction<@NotNull C> factory) {
    return this.findAllSync(modelType -> {}, factory);
  }

  /**
   * Finds all the models in the repository and returns them in the specified {@link Collection}, it also executes
   * the specified action for each model after it's loaded, so you mustn't iterate over the returned {@link Collection}.
   *
   * @param postLoadAction The action to execute for each {@link ModelType} after it's loaded.
   * @param factory        The factory to create the {@link Collection} to return.
   * @param <C>            The type of the {@link Collection} to return.
   * @return A {@link Collection} containing all the {@link ModelType} in the repository or {@code null} if the repository is empty.
   * @since 1.0.0
   */
  <C extends Collection<@NotNull ModelType>> @Nullable C findAllSync(final @NotNull Consumer<@NotNull ModelType> postLoadAction, final @NotNull IntFunction<@NotNull C> factory);

  /**
   * Iterates over all the {@link ModelType}s in the repository and executes the specified action for each one.
   *
   * @param action The action to execute for each {@link ModelType}.
   * @since 1.0.0
   */
  void forEachSync(final @NotNull Consumer<@NotNull ModelType> action);

  /**
   * Checks if the {@link ModelType} with the specified id exists in the repository.
   *
   * @param id The id of the {@link ModelType}.
   * @return {@code true} if the {@link ModelType} with the specified id exists, {@code false} otherwise.
   * @since 1.0.0
   */
  boolean existsSync(final @NotNull String id);

  /**
   * Saves the specified {@link ModelType} in the repository.
   *
   * @param model The {@link ModelType} to save.
   * @return The saved {@link ModelType}.
   * @since 1.0.0
   */
  @Contract("_ -> param1")
  @NotNull ModelType saveSync(final @NotNull ModelType model);

  /**
   * Deletes the specified {@link ModelType} from the repository. Note that this method doesn't return
   * the deleted {@link ModelType}, if you want to retrieve the deleted {@link ModelType} use
   * {@link #deleteAndRetrieveSync(String)}. Also, this method can delete permanently the {@link ModelType}
   * from the repository, so it's important to note that this method can be dangerous if you don't
   * know what you're doing.
   *
   * @param id The id of the {@link ModelType} to delete.
   * @return {@code true} if the {@link ModelType} was deleted successfully, {@code false} otherwise.
   * @since 1.0.0
   */
  boolean deleteSync(final @NotNull String id);

  /**
   * Deletes the specified {@link ModelType} from the repository and returns it.
   *
   * @param id The id of the {@link ModelType} to delete.
   * @return The deleted {@link ModelType}, or {@code null} if it doesn't exist.
   * @since 1.0.0
   */
  @Nullable ModelType deleteAndRetrieveSync(final @NotNull String id);

  /**
   * Deletes all the {@link ModelType}s from the repository.
   *
   * @since 1.0.0
   */
  void deleteAllSync();
}
