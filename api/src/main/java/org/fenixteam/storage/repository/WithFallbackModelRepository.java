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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import org.fenixteam.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is a {@link ModelRepository} that uses two {@link ModelRepository}s to store the {@link ModelType}s.
 * The first one is the {@link #mainModelRepository}, which is used to store the {@link ModelType}s in a persistent way.
 * The second one is the {@link #fallbackModelRepository}, which is mainly used to cache the {@link ModelType}s.
 *
 * @param <ModelType> The type of the {@link Model} that this {@link ModelRepository} will store.
 * @see ModelRepository
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class WithFallbackModelRepository<ModelType extends Model> extends AsyncModelRepository<ModelType> {
  /**
   * The {@link ModelRepository} that will be used as a fallback if the {@link #mainModelRepository} doesn't have the
   * {@link ModelType} that we're looking for. Mainly used to cache the {@link ModelType}s.
   */
  protected final ModelRepository<ModelType> fallbackModelRepository;
  /**
   * The {@link ModelRepository} that will be used as the main repository. Mainly used to store the {@link ModelType}s
   * in a persistent way.
   */
  protected final ModelRepository<ModelType> mainModelRepository;

  /**
   * This constructor creates a new {@link WithFallbackModelRepository} with the specified {@link Executor}, {@link ModelRepository}
   * as the {@link #fallbackModelRepository} and {@link #mainModelRepository}.
   *
   * @param executor                The {@link Executor} to use.
   * @param fallbackModelRepository The {@link ModelRepository} to use as the {@link #fallbackModelRepository}.
   * @param mainModelRepository     The {@link ModelRepository} to use as the {@link #mainModelRepository}.
   * @since 1.0.0
   */
  public WithFallbackModelRepository(final @NotNull Executor executor, final @NotNull ModelRepository<ModelType> fallbackModelRepository, final @NotNull ModelRepository<ModelType> mainModelRepository) {
    super(executor);
    this.fallbackModelRepository = fallbackModelRepository;
    this.mainModelRepository = mainModelRepository;
  }

  /**
   * This method uses the {@link ModelRepository#findSync(String)} of the {@link #mainModelRepository} to find the
   * {@link ModelType} with the specified id.
   *
   * @param id The id of the {@link ModelType}.
   * @return The {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   * @see ModelRepository#findSync(String)
   * @since 1.0.0
   */
  @Override
  public @Nullable ModelType findSync(final @NotNull String id) {
    return this.mainModelRepository.findSync(id);
  }

  /**
   * This method uses the {@link ModelRepository#findIdsSync()} of the {@link #mainModelRepository} to find the
   * ids of the repository.
   *
   * @return A {@link Collection} containing all the ids of the repository.
   * @see ModelRepository#findIdsSync()
   * @since 1.0.0
   */
  @Override
  public @Nullable Collection<@NotNull String> findIdsSync() {
    return this.mainModelRepository.findIdsSync();
  }

  /**
   * This method uses the {@link ModelRepository#findIdsSync(IntFunction)} of the {@link #mainModelRepository} to find the
   * ids of the repository.
   *
   * @param factory The factory to create the {@link Collection} to return.
   * @param <C>     The type of the {@link Collection} to return.
   * @return A {@link Collection} containing all the ids of the repository.
   * @see ModelRepository#findIdsSync(IntFunction)
   * @since 1.0.0
   */
  @Override
  public <C extends Collection<@NotNull String>> @Nullable C findIdsSync(final @NotNull IntFunction<@NotNull C> factory) {
    return this.mainModelRepository.findIdsSync(factory);
  }

  /**
   * This method uses the {@link ModelRepository#findAllSync(IntFunction)} of the {@link #mainModelRepository} to find the
   * {@link ModelType}s in the repository.
   *
   * @param postLoadAction The action to execute for each model after it's loaded.
   * @param factory        The factory to create the {@link Collection} to return.
   * @param <C>            The type of the {@link Collection} to return.
   * @return A {@link Collection} containing all the {@link ModelType}s in the repository.
   * @see ModelRepository#findAllSync(IntFunction)
   * @since 1.0.0
   */
  @Override
  public <C extends Collection<@NotNull ModelType>> @Nullable C findAllSync(final @NotNull Consumer<ModelType> postLoadAction, final @NotNull IntFunction<@NotNull C> factory) {
    return this.mainModelRepository.findAllSync(postLoadAction, factory);
  }

  /**
   * This method uses the {@link ModelRepository#forEachSync(Consumer)} of the {@link #mainModelRepository} to iterate
   * over the {@link ModelType}s in the {@link #mainModelRepository}.
   *
   * @param action The action to execute for each model.
   * @see ModelRepository#forEachSync(Consumer)
   * @since 1.0.0
   */
  @Override
  public void forEachSync(final @NotNull Consumer<@NotNull ModelType> action) {
    this.mainModelRepository.forEachSync(action);
  }

  /**
   * This method uses the {@link ModelRepository#existsSync(String)} of the {@link #mainModelRepository} to check if
   * the {@link ModelType} with the specified id exists.
   *
   * @param id The id of the model.
   * @return {@code true} if the model exists, {@code false} otherwise.
   * @see ModelRepository#existsSync(String)
   * @since 1.0.0
   */
  @Override
  public boolean existsSync(final @NotNull String id) {
    return this.mainModelRepository.existsSync(id);
  }

  /**
   * This method uses the {@link ModelRepository#saveSync(Model)} of the {@link #mainModelRepository} to save the
   * {@link ModelType}.
   *
   * @param model The {@link Model} to save. It must have an id.
   * @return The saved {@link ModelType}.
   * @see ModelRepository#saveSync(Model)
   * @since 1.0.0
   */
  @Override
  public @NotNull ModelType saveSync(final @NotNull ModelType model) {
    return this.mainModelRepository.saveSync(model);
  }

  /**
   * This method uses the {@link ModelRepository#deleteSync(String)} of the {@link #mainModelRepository} to delete the
   * {@link ModelType} with the specified id.
   *
   * @param id The id of the {@link Model} to delete.
   * @return {@code true} if the {@link ModelType} was deleted successfully, {@code false} otherwise.
   * @see ModelRepository#deleteSync(String)
   * @since 1.0.0
   */
  @Override
  public boolean deleteSync(final @NotNull String id) {
    return this.mainModelRepository.deleteSync(id);
  }

  /**
   * This method uses the {@link ModelRepository#deleteAndRetrieveSync(String)} of the {@link #mainModelRepository} to delete the
   * {@link ModelType} with the specified id.
   *
   * @param id The id of the {@link Model} to delete.
   * @return The deleted {@link ModelType}, or {@code null} if it doesn't exist.
   * @see ModelRepository#deleteAndRetrieveSync(String)
   * @since 1.0.0
   */
  @Override
  public @Nullable ModelType deleteAndRetrieveSync(final @NotNull String id) {
    return this.mainModelRepository.deleteAndRetrieveSync(id);
  }

  /**
   * This method uses the {@link ModelRepository#deleteAllSync()} of the {@link #mainModelRepository} to delete all the
   * {@link ModelType}s in the {@link #mainModelRepository}.
   *
   * @see ModelRepository#deleteAllSync()
   * @since 1.0.0
   */
  @Override
  public void deleteAllSync() {
    this.mainModelRepository.deleteAllSync();
  }

  /**
   * This method returns the {@link #fallbackModelRepository}.
   *
   * @return The {@link #fallbackModelRepository}.
   * @since 1.0.0
   */
  public @NotNull ModelRepository<ModelType> fallbackModelRepository() {
    return this.fallbackModelRepository;
  }

  /**
   * This method returns the {@link #mainModelRepository}.
   *
   * @return The {@link #mainModelRepository}.
   * @since 1.0.0
   */
  public @NotNull ModelRepository<ModelType> mainModelRepository() {
    return this.mainModelRepository;
  }

  /**
   * This method uses the {@link ModelRepository#findSync(String)} of the {@link #mainModelRepository} to find the
   * {@link ModelType} with the specified id, and if it exists, it saves it to the {@link #fallbackModelRepository}.
   * If it doesn't exist, it returns {@code null}.
   *
   * @param id The id of the {@link ModelType}.
   * @return The {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   * @see ModelRepository#findSync(String)
   * @see ModelRepository#saveSync(Model)
   * @since 1.0.0
   */
  public @Nullable ModelType findAndSaveToFallbackSync(final @NotNull String id) {
    final var model = this.findSync(id);
    if (model == null) {
      return null;
    }
    this.fallbackModelRepository.saveSync(model);
    return model;
  }

  /**
   * This method executes and wraps the {@link #findAndSaveToFallbackSync(String)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param id The id of the {@link ModelType}.
   * @return A {@link CompletableFuture} that will complete with the {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   * @see #findAndSaveToFallbackSync(String)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@Nullable ModelType> findAndSaveToFallback(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.findAndSaveToFallbackSync(id), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#findSync(String)} of the {@link #fallbackModelRepository} to find the
   * {@link ModelType} with the specified id.
   *
   * @param id The id of the {@link ModelType}.
   * @return The {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   * @see ModelRepository#findSync(String)
   * @since 1.0.0
   */
  public @Nullable ModelType findInFallbackSync(final @NotNull String id) {
    return this.fallbackModelRepository.findSync(id);
  }

  /**
   * This method executes and wraps the {@link #findInFallbackSync(String)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param id The id of the {@link ModelType}.
   * @return A {@link CompletableFuture} that will complete with the {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   * @see #findInFallbackSync(String)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@Nullable ModelType> findInFallback(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.findInFallbackSync(id), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#findSync(String)} of the {@link #fallbackModelRepository} to find the
   * {@link ModelType} with the specified id, and if it doesn't exist, it uses the {@link ModelRepository#findSync(String)}
   * of the {@link #mainModelRepository} to find the {@link ModelType} with the specified id.
   *
   * @param id The id of the {@link ModelType}.
   * @return The {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   * @see ModelRepository#findSync(String)
   * @since 1.0.0
   */
  public @Nullable ModelType findInBothSync(final @NotNull String id) {
    final var model = this.findInFallbackSync(id);
    if (model != null) {
      return model;
    }
    return this.findSync(id);
  }

  /**
   * This method executes and wraps the {@link #findInBothSync(String)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param id The id of the {@link ModelType}.
   * @return A {@link CompletableFuture} that will complete with the {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   * @see #findInBothSync(String)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@Nullable ModelType> findInBoth(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.findInBothSync(id), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#findSync(String)} of the {@link #fallbackModelRepository} to find the
   * {@link ModelType} with the specified id, and if it doesn't exist, it uses the {@link ModelRepository#findSync(String)}
   * of the {@link #mainModelRepository} to find the {@link ModelType} with the specified id, and if it exists, it saves
   * it to the {@link #fallbackModelRepository}.
   *
   * @param id The id of the {@link ModelType}.
   * @return The {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   * @see ModelRepository#findSync(String)
   * @see ModelRepository#saveSync(Model)
   * @since 1.0.0
   */
  public @Nullable ModelType findInBothAndSaveToFallbackSync(final @NotNull String id) {
    final var cachedModel = this.findInFallbackSync(id);
    if (cachedModel != null) {
      return cachedModel;
    }
    final var foundModel = this.findSync(id);
    if (foundModel == null) {
      return null;
    }
    this.fallbackModelRepository.saveSync(foundModel);
    return foundModel;
  }

  /**
   * This method executes and wraps the {@link #findInBothAndSaveToFallbackSync(String)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param id The id of the {@link ModelType}.
   * @return A {@link CompletableFuture} that will complete with the {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   * @see #findInBothAndSaveToFallbackSync(String)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@Nullable ModelType> findInBothAndSaveToFallback(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.findInBothAndSaveToFallbackSync(id), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#findIdsSync()} of the {@link #fallbackModelRepository} to find the
   * ids of the repository.
   *
   * @return A {@link Collection} containing all the ids of the repository.
   * @see ModelRepository#findIdsSync()
   * @since 1.0.0
   */
  public @Nullable Collection<@NotNull String> findAllIdsInFallbackSync() {
    return this.fallbackModelRepository.findIdsSync();
  }

  /**
   * This method executes and wraps the {@link #findAllIdsInFallbackSync()} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @return A {@link CompletableFuture} that will complete with a {@link Collection} containing all the ids of the repository.
   * @see #findAllIdsInFallbackSync()
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@Nullable Collection<@NotNull String>> findAllIdsInFallback() {
    return CompletableFuture.supplyAsync(this::findAllIdsInFallbackSync, this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#findAllSync(IntFunction)} of the {@link #fallbackModelRepository} to find the
   * {@link ModelType}s in the repository.
   *
   * @param factory The factory to create the {@link Collection} to return.
   * @param <C>     The type of the {@link Collection} to return.
   * @return A {@link Collection} containing all the {@link ModelType}s in the repository.
   * @see ModelRepository#findAllSync(IntFunction)
   * @since 1.0.0
   */
  public <C extends Collection<@NotNull ModelType>> @Nullable C findAllInFallbackSync(final @NotNull IntFunction<@NotNull C> factory) {
    return this.fallbackModelRepository.findAllSync(factory);
  }

  /**
   * This method executes and wraps the {@link #findAllInFallbackSync(IntFunction)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param factory The factory to create the {@link Collection} to return.
   * @param <C>     The type of the {@link Collection} to return.
   * @return A {@link CompletableFuture} that will complete with a {@link Collection} containing all the {@link ModelType}s in the repository.
   * @see #findAllInFallbackSync(IntFunction)
   * @since 1.0.0
   */
  public <C extends Collection<@NotNull ModelType>> @NotNull CompletableFuture<@Nullable C> findAllInFallback(final @NotNull IntFunction<@NotNull C> factory) {
    return CompletableFuture.supplyAsync(() -> this.findAllInFallbackSync(factory), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#findAllSync(Consumer, IntFunction)} of the {@link #fallbackModelRepository} to find the
   * {@link ModelType}s in the repository.
   *
   * @param postLoadAction The action to execute for each model after it's loaded.
   * @param factory        The factory to create the {@link Collection} to return.
   * @param <C>            The type of the {@link Collection} to return.
   * @return A {@link Collection} containing all the {@link ModelType}s in the repository.
   * @see ModelRepository#findAllSync(Consumer, IntFunction)
   * @since 1.0.0
   */
  public <C extends Collection<@NotNull ModelType>> @Nullable C findAllInFallbackSync(final @NotNull Consumer<@NotNull ModelType> postLoadAction, final @NotNull IntFunction<@NotNull C> factory) {
    return this.fallbackModelRepository.findAllSync(postLoadAction, factory);
  }

  /**
   * This method executes and wraps the {@link #findAllInFallbackSync(Consumer, IntFunction)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param postLoadAction The action to execute for each model after it's loaded.
   * @param factory        The factory to create the {@link Collection} to return.
   * @param <C>            The type of the {@link Collection} to return.
   * @return A {@link CompletableFuture} that will complete with a {@link Collection} containing all the {@link ModelType}s in the repository.
   * @see #findAllInFallbackSync(Consumer, IntFunction)
   * @since 1.0.0
   */
  public <C extends Collection<@NotNull ModelType>> @NotNull CompletableFuture<@Nullable C> findAllInFallback(final @NotNull Consumer<@NotNull ModelType> postLoadAction, final @NotNull IntFunction<@NotNull C> factory) {
    return CompletableFuture.supplyAsync(() -> this.findAllInFallbackSync(postLoadAction, factory), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#forEachSync(Consumer)} of the {@link #fallbackModelRepository} to iterate
   * over the {@link ModelType}s in the {@link #fallbackModelRepository}.
   *
   * @param action The action to execute for each {@link ModelType}.
   * @see ModelRepository#forEachSync(Consumer)
   * @since 1.0.0
   */
  public void forEachInFallbackSync(final @NotNull Consumer<@NotNull ModelType> action) {
    this.fallbackModelRepository.forEachSync(action);
  }

  /**
   * This method executes and wraps the {@link #forEachInFallbackSync(Consumer)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param action The action to execute for each {@link ModelType}.
   * @see #forEachInFallbackSync(Consumer)
   * @since 1.0.0
   */
  public void forEachInFallback(final @NotNull Consumer<@NotNull ModelType> action) {
    CompletableFuture.runAsync(() -> this.forEachInFallbackSync(action), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#findAllSync(Consumer, IntFunction)} of the {@link #mainModelRepository} to find the
   * {@link ModelType}s in the repository, and if they exist, it saves them to the {@link #fallbackModelRepository}.
   *
   * @param postLoadAction The action to execute for each model after it's loaded.
   * @param factory        The factory to create the {@link Collection} to return.
   * @param <C>            The type of the {@link Collection} to return.
   * @return A {@link Collection} containing all the {@link ModelType}s in the repository.
   * @see ModelRepository#findAllSync(Consumer, IntFunction)
   * @since 1.0.0
   */
  public <C extends Collection<@NotNull ModelType>> @Nullable C loadAllSync(final @NotNull Consumer<@NotNull ModelType> postLoadAction, final @NotNull IntFunction<@NotNull C> factory) {
    final var models = this.mainModelRepository.findAllSync(postLoadAction, factory);
    if (models == null) {
      return null;
    }
    for (final var model : models) {
      this.fallbackModelRepository.saveSync(model);
    }
    return models;
  }

  /**
   * This method executes and wraps the {@link #loadAllSync(Consumer, IntFunction)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param postLoadAction The action to execute for each model after it's loaded.
   * @param factory        The factory to create the {@link Collection} to return.
   * @param <C>            The type of the {@link Collection} to return.
   * @return A {@link CompletableFuture} that will complete with a {@link Collection} containing all the {@link ModelType}s in the repository.
   * @see #loadAllSync(Consumer, IntFunction)
   * @since 1.0.0
   */
  public <C extends Collection<@NotNull ModelType>> @NotNull CompletableFuture<@Nullable C> loadAll(final @NotNull Consumer<@NotNull ModelType> postLoadAction, final @NotNull IntFunction<@NotNull C> factory) {
    return CompletableFuture.supplyAsync(() -> this.loadAllSync(postLoadAction, factory), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#deleteAndRetrieveSync(String)} of the {@link #fallbackModelRepository} to delete the
   * {@link ModelType} with the specified id, and if it exists, it saves it to the {@link #mainModelRepository}.
   *
   * @param id The id of the {@link ModelType}.
   * @return The {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   * @see ModelRepository#deleteAndRetrieveSync(String)
   * @see ModelRepository#saveSync(Model)
   * @since 1.0.0
   */
  public @Nullable ModelType uploadSync(final @NotNull String id) {
    final var modelType = this.fallbackModelRepository.deleteAndRetrieveSync(id);
    if (modelType == null) {
      return null;
    }
    this.mainModelRepository.saveSync(modelType);
    return modelType;
  }

  /**
   * This method executes and wraps the {@link #uploadSync(String)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param id The id of the {@link ModelType}.
   * @return A {@link CompletableFuture} that will complete with the {@link ModelType} with the specified id, or {@code null} if it doesn't exist.
   * @see #uploadSync(String)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@Nullable ModelType> upload(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.uploadSync(id), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#forEachSync(Consumer)} of the {@link #fallbackModelRepository} to iterate
   * over the {@link ModelType}s in the {@link #fallbackModelRepository}, and if they exist, it saves them to the
   * {@link #mainModelRepository}. After that, it deletes all the {@link ModelType}s from the {@link #fallbackModelRepository}.
   *
   * @param preUploadAction The action to execute for each model before it's uploaded.
   * @see ModelRepository#forEachSync(Consumer)
   * @see ModelRepository#saveSync(Model)
   * @see ModelRepository#deleteAllSync()
   * @since 1.0.0
   */
  public void uploadAllSync(final @NotNull Consumer<@NotNull ModelType> preUploadAction) {
    this.fallbackModelRepository.forEachSync(modelType -> {
      preUploadAction.accept(modelType);
      this.mainModelRepository.saveSync(modelType);
    });
    this.fallbackModelRepository.deleteAllSync();
  }

  /**
   * This method executes and wraps the {@link #uploadAllSync(Consumer)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param preUploadAction The action to execute for each model before it's uploaded.
   * @see #uploadAllSync(Consumer)
   * @since 1.0.0
   */
  public void uploadAll(final @NotNull Consumer<@NotNull ModelType> preUploadAction) {
    CompletableFuture.runAsync(() -> this.uploadAllSync(preUploadAction), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#existsSync(String)} of the {@link #fallbackModelRepository} to check if
   * the {@link ModelType} with the specified id exists.
   *
   * @param id The id of the {@link ModelType}.
   * @return {@code true} if the {@link ModelType} with the specified id exists in the {@link #fallbackModelRepository}, {@code false} otherwise.
   * @see ModelRepository#existsSync(String)
   * @since 1.0.0
   */
  public boolean existsInFallbackSync(final @NotNull String id) {
    return this.fallbackModelRepository.existsSync(id);
  }

  /**
   * This method executes and wraps the {@link #existsInFallbackSync(String)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param id The id of the {@link ModelType}.
   * @return A {@link CompletableFuture} that will complete with {@code true} if the {@link ModelType} with the specified id exists in the {@link #fallbackModelRepository}, {@code false} otherwise.
   * @see #existsInFallbackSync(String)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@NotNull Boolean> existsInFallback(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.existsInFallbackSync(id), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#existsSync(String)} of the {@link #fallbackModelRepository} to check if
   * the {@link ModelType} with the specified id exists, and if it doesn't, it uses the {@link ModelRepository#existsSync(String)}
   * of the {@link #mainModelRepository} to check if the {@link ModelType} with the specified id exists.
   *
   * @param id The id of the {@link ModelType}.
   * @return {@code true} if the {@link ModelType} with the specified id exists in the {@link #fallbackModelRepository} or in the {@link #mainModelRepository}, {@code false} otherwise.
   * @see ModelRepository#existsSync(String)
   * @since 1.0.0
   */
  public boolean existsInAnySync(final @NotNull String id) {
    return this.existsInFallbackSync(id) || this.mainModelRepository.existsSync(id);
  }

  /**
   * This method executes and wraps the {@link #existsInAnySync(String)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param id The id of the {@link ModelType}.
   * @return A {@link CompletableFuture} that will complete with {@code true} if the {@link ModelType} with the specified id exists in the {@link #fallbackModelRepository} or in the {@link #mainModelRepository}, {@code false} otherwise.
   * @see #existsInAnySync(String)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@NotNull Boolean> existsInAny(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.existsInAnySync(id), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#existsSync(String)} of the {@link #fallbackModelRepository} and the
   * {@link ModelRepository#existsSync(String)} of the {@link #mainModelRepository} to check if the {@link ModelType}
   * with the specified id exists in both repositories.
   *
   * @param id The id of the {@link ModelType}.
   * @return {@code true} if the {@link ModelType} with the specified id exists in the {@link #fallbackModelRepository} and in the {@link #mainModelRepository}, {@code false} otherwise.
   * @see ModelRepository#existsSync(String)
   * @since 1.0.0
   */
  public boolean existsInBothSync(final @NotNull String id) {
    return this.existsInFallbackSync(id) && this.mainModelRepository.existsSync(id);
  }

  /**
   * This method executes and wraps the {@link #existsInBothSync(String)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param id The id of the {@link ModelType}.
   * @return A {@link CompletableFuture} that will complete with {@code true} if the {@link ModelType} with the specified id exists in the {@link #fallbackModelRepository} and in the {@link #mainModelRepository}, {@code false} otherwise.
   * @see #existsInBothSync(String)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@NotNull Boolean> existsInBoth(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.existsInBothSync(id), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#saveSync(Model)} of the {@link #fallbackModelRepository} to save the
   * {@link ModelType}.
   *
   * @param model The {@link ModelType} to save. It must have an id.
   * @return The saved {@link ModelType}.
   * @see ModelRepository#saveSync(Model)
   * @since 1.0.0
   */
  @Contract("_ -> param1")
  public @NotNull ModelType saveInFallbackSync(final @NotNull ModelType model) {
    this.fallbackModelRepository.saveSync(model);
    return model;
  }

  /**
   * This method executes and wraps the {@link #saveInFallbackSync(Model)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param model The {@link ModelType} to save. It must have an id.
   * @return A {@link CompletableFuture} that will complete with the saved {@link ModelType}.
   * @see #saveInFallbackSync(Model)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@NotNull ModelType> saveInFallback(final @NotNull ModelType model) {
    return CompletableFuture.supplyAsync(() -> this.saveInFallbackSync(model), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#saveSync(Model)} of the {@link #mainModelRepository} and the
   * {@link ModelRepository#saveSync(Model)} of the {@link #fallbackModelRepository} to save the {@link ModelType}.
   *
   * @param model The {@link ModelType} to save. It must have an id.
   * @return The saved {@link ModelType}.
   * @see ModelRepository#saveSync(Model)
   * @since 1.0.0
   */
  @Contract("_ -> param1")
  public @NotNull ModelType saveInBothSync(final @NotNull ModelType model) {
    this.fallbackModelRepository.saveSync(model);
    this.mainModelRepository.saveSync(model);
    return model;
  }

  /**
   * This method executes and wraps the {@link #saveInBothSync(Model)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param model The {@link ModelType} to save. It must have an id.
   * @return A {@link CompletableFuture} that will complete with the saved {@link ModelType}.
   * @see #saveInBothSync(Model)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@NotNull ModelType> saveInBoth(final @NotNull ModelType model) {
    return CompletableFuture.supplyAsync(() -> this.saveInBothSync(model), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#deleteSync(String)} of the {@link #fallbackModelRepository} to delete the
   * {@link ModelType} with the specified id.
   *
   * @param id The id of the {@link ModelType} to delete.
   * @return {@code true} if the {@link ModelType} was deleted successfully, {@code false} otherwise.
   * @see ModelRepository#deleteSync(String)
   * @since 1.0.0
   */
  public boolean deleteInFallbackSync(final @NotNull String id) {
    return this.fallbackModelRepository.deleteSync(id);
  }

  /**
   * This method executes and wraps the {@link #deleteInFallbackSync(String)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param id The id of the {@link ModelType} to delete.
   * @return A {@link CompletableFuture} that will complete with {@code true} if the {@link ModelType} was deleted successfully, {@code false} otherwise.
   * @see #deleteInFallbackSync(String)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@NotNull Boolean> deleteInFallback(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.deleteInFallbackSync(id), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#deleteSync(String)} of the {@link #fallbackModelRepository} and the
   * {@link ModelRepository#deleteSync(String)} of the {@link #mainModelRepository} to delete the {@link ModelType}
   * with the specified id.
   *
   * @param id The id of the {@link ModelType} to delete.
   * @return {@code true} if the {@link ModelType} was deleted successfully in both repositories, {@code false} otherwise.
   * @see ModelRepository#deleteSync(String)
   * @since 1.0.0
   */
  public boolean deleteInBothSync(final @NotNull String id) {
    return this.fallbackModelRepository.deleteSync(id) && this.mainModelRepository.deleteSync(id);
  }

  /**
   * This method executes and wraps the {@link #deleteInBothSync(String)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param id The id of the {@link ModelType} to delete.
   * @return A {@link CompletableFuture} that will complete with {@code true} if the {@link ModelType} was deleted successfully in both repositories, {@code false} otherwise.
   * @see #deleteInBothSync(String)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@NotNull Boolean> deleteInBoth(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.deleteInBothSync(id), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#deleteAndRetrieveSync(String)} of the {@link #fallbackModelRepository} to delete the
   * {@link ModelType} with the specified id.
   *
   * @param id The id of the {@link ModelType} to delete.
   * @return The deleted {@link ModelType}, or {@code null} if it doesn't exist.
   * @see ModelRepository#deleteAndRetrieveSync(String)
   * @since 1.0.0
   */
  public @Nullable ModelType deleteAndRetrieveInFallbackSync(final @NotNull String id) {
    return this.fallbackModelRepository.deleteAndRetrieveSync(id);
  }

  /**
   * This method executes and wraps the {@link #deleteAndRetrieveInFallbackSync(String)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param id The id of the {@link ModelType} to delete.
   * @return A {@link CompletableFuture} that will complete with the deleted {@link ModelType}, or {@code null} if it doesn't exist.
   * @see #deleteAndRetrieveInFallbackSync(String)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@Nullable ModelType> deleteAndRetrieveInFallback(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.deleteAndRetrieveInFallbackSync(id), this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#deleteAllSync()} of the {@link #fallbackModelRepository} to delete all the
   * {@link ModelType}s in the repository.
   *
   * @see ModelRepository#deleteAllSync()
   * @since 1.0.0
   */
  public void deleteAllInFallbackSync() {
    this.fallbackModelRepository.deleteAllSync();
  }

  /**
   * This method executes and wraps the {@link #deleteAllInFallbackSync()} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @return A {@link CompletableFuture} that will complete when all the {@link ModelType}s in the repository are deleted.
   * @see #deleteAllInFallbackSync()
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@NotNull Void> deleteAllInFallback() {
    return CompletableFuture.runAsync(this::deleteAllInFallbackSync, this.executor);
  }

  /**
   * This method uses the {@link ModelRepository#forEachSync(Consumer)} of the {@link #fallbackModelRepository} to iterate
   * over the {@link ModelType}s in the {@link #fallbackModelRepository} and saves them to the {@link #mainModelRepository}.
   *
   * @param preSaveAction The action to execute for each model before it's saved.
   * @see ModelRepository#forEachSync(Consumer)
   * @see ModelRepository#saveSync(Model)
   * @since 1.0.0
   */
  public void saveAllSync(final @NotNull Consumer<ModelType> preSaveAction) {
    this.fallbackModelRepository.forEachSync(modelType -> {
      preSaveAction.accept(modelType);
      this.mainModelRepository.saveSync(modelType);
    });
  }

  /**
   * This method executes and wraps the {@link #saveAllSync(Consumer)} method in a {@link CompletableFuture} with
   * the {@link Executor} specified in the constructor.
   *
   * @param preSaveAction The action to execute for each {@link ModelType} before it's saved.
   * @return A {@link CompletableFuture} that will complete when all the {@link ModelType}s in the repository are saved.
   * @see #saveAllSync(Consumer)
   * @since 1.0.0
   */
  public @NotNull CompletableFuture<@NotNull Void> saveAll(final @NotNull Consumer<ModelType> preSaveAction) {
    return CompletableFuture.runAsync(() -> this.saveAllSync(preSaveAction), this.executor);
  }
}