package es.revengenetwork.storage.repository;

import es.revengenetwork.storage.model.Model;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class CachedModelRepository<ModelType extends Model> extends AbstractAsyncModelRepository<ModelType> {
  protected final ModelRepository<ModelType> cacheModelRepository;
  protected final ModelRepository<ModelType> persistModelRepository;

  public CachedModelRepository(
    final @NotNull Executor executor,
    final @NotNull ModelRepository<ModelType> cacheModelRepository,
    final @NotNull ModelRepository<ModelType> persistModelRepository
  ) {
    super(executor);
    this.cacheModelRepository = cacheModelRepository;
    this.persistModelRepository = persistModelRepository;
  }

  public @NotNull ModelRepository<ModelType> cacheModelRepository() {
    return this.cacheModelRepository;
  }

  public @NotNull ModelRepository<ModelType> persistModelRepository() {
    return this.persistModelRepository;
  }

  public @Nullable ModelType findAndCacheSync(final @NotNull String id) {
    final var model = this.findSync(id);
    if (model == null) {
      return null;
    }
    this.cacheModelRepository.saveSync(model);
    return model;
  }

  public @Nullable ModelType findInCacheSync(final @NotNull String id) {
    return this.cacheModelRepository.findSync(id);
  }

  public @Nullable ModelType findInBothSync(final @NotNull String id) {
    final var model = this.findInCacheSync(id);
    if (model != null) {
      return model;
    }
    return this.findSync(id);
  }

  public @Nullable ModelType findInBothAndCacheSync(final @NotNull String id) {
    final var cachedModel = this.findInCacheSync(id);
    if (cachedModel != null) {
      return cachedModel;
    }
    final var foundModel = this.findSync(id);
    if (foundModel == null) {
      return null;
    }
    this.cacheModelRepository.saveSync(foundModel);
    return foundModel;
  }

  public @Nullable Collection<String> findAllCachedIdsSync() {
    return this.cacheModelRepository.findIdsSync();
  }

  public <C extends Collection<ModelType>> @Nullable C findAllCachedSync(final @NotNull Function<Integer, C> factory) {
    return this.cacheModelRepository.findAllSync(factory);
  }

  public <C extends Collection<ModelType>> @Nullable C findAllCachedSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    return this.cacheModelRepository.findAllSync(postLoadAction, factory);
  }

  public <C extends Collection<ModelType>> @Nullable C loadAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    final var models = this.persistModelRepository.findAllSync(postLoadAction, factory);
    if (models == null) {
      return null;
    }
    for (final var model : models) {
      this.cacheModelRepository.saveSync(model);
    }
    return models;
  }

  @Contract("_ -> param1")
  public @NotNull ModelType uploadSync(final @NotNull ModelType model) {
    this.cacheModelRepository.deleteSync(model);
    this.persistModelRepository.saveSync(model);
    return model;
  }

  public void uploadAllSync(final @NotNull Consumer<ModelType> preUploadAction) {
    this.cacheModelRepository.findAllSync(
      model -> {
        preUploadAction.accept(model);
        this.cacheModelRepository.deleteSync(model);
        this.persistModelRepository.saveSync(model);
      },
      ArrayList::new);
  }

  public boolean existsInCacheSync(final @NotNull String id) {
    return this.cacheModelRepository.existsSync(id);
  }

  public boolean existsInCacheOrPersistentSync(final @NotNull String id) {
    return this.existsInCacheSync(id) || this.persistModelRepository.existsSync(id);
  }

  public boolean existsInBothSync(final @NotNull String id) {
    return this.existsInCacheSync(id) && this.persistModelRepository.existsSync(id);
  }

  @Contract("_ -> param1")
  public @NotNull ModelType saveInCacheSync(final @NotNull ModelType model) {
    this.cacheModelRepository.saveSync(model);
    return model;
  }

  @Contract("_ -> param1")
  public @NotNull ModelType saveInBothSync(final @NotNull ModelType model) {
    this.cacheModelRepository.saveSync(model);
    this.persistModelRepository.saveSync(model);
    return model;
  }

  public boolean deleteInCacheSync(final @NotNull String id) {
    return this.cacheModelRepository.deleteSync(id);
  }

  public boolean deleteInBothSync(final @NotNull String id) {
    return this.cacheModelRepository.deleteSync(id) &&
           this.persistModelRepository.deleteSync(id);
  }

  public void saveAllSync(final @NotNull Consumer<ModelType> preSaveAction) {
    this.cacheModelRepository.findAllSync(
      model -> {
        preSaveAction.accept(model);
        this.persistModelRepository.saveSync(model);
      },
      ArrayList::new);
  }

  @Override
  public @Nullable ModelType findSync(final @NotNull String id) {
    return this.persistModelRepository.findSync(id);
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    return this.persistModelRepository.findIdsSync();
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    return this.persistModelRepository.findAllSync(postLoadAction, factory);
  }

  @Override
  public boolean existsSync(final @NotNull String id) {
    return this.persistModelRepository.existsSync(id);
  }

  @Override
  public @NotNull ModelType saveSync(final @NotNull ModelType model) {
    return this.persistModelRepository.saveSync(model);
  }

  @Override
  public boolean deleteSync(final @NotNull String id) {
    return this.persistModelRepository.deleteSync(id);
  }

  public @NotNull CompletableFuture<@Nullable ModelType> findAndCache(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.findAndCacheSync(id), super.executor);
  }

  public @NotNull CompletableFuture<@Nullable ModelType> findInCache(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.findInCacheSync(id), super.executor);
  }

  public @NotNull CompletableFuture<@Nullable ModelType> findInBoth(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.findInBothSync(id), super.executor);
  }

  public @NotNull CompletableFuture<@Nullable ModelType> findInBothAndCache(
    final @NotNull String id
  ) {
    return CompletableFuture.supplyAsync(() -> this.findInBothAndCacheSync(id), super.executor);
  }

  public @NotNull CompletableFuture<@Nullable Collection<String>> findAllCachedIds() {
    return CompletableFuture.supplyAsync(this::findAllCachedIdsSync, super.executor);
  }

  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> findAllCached(
    final @NotNull Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> this.findAllCachedSync(factory), super.executor);
  }

  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> findAllCached(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> this.findAllCachedSync(postLoadAction, factory), super.executor);
  }

  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> loadAll(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> this.loadAllSync(postLoadAction, factory), super.executor);
  }

  public @NotNull CompletableFuture<@NotNull ModelType> upload(final @NotNull ModelType model) {
    return CompletableFuture.supplyAsync(() -> this.uploadSync(model), super.executor);
  }

  public @NotNull CompletableFuture<Void> uploadAll() {
    return this.uploadAll(modelType -> { });
  }

  public @NotNull CompletableFuture<Void> uploadAll(final @NotNull Consumer<ModelType> preUploadAction) {
    return CompletableFuture.runAsync(() -> this.uploadAllSync(preUploadAction), super.executor);
  }

  public @NotNull CompletableFuture<@NotNull Boolean> existsInCache(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.existsInCacheSync(id), super.executor);
  }

  public @NotNull CompletableFuture<@NotNull Boolean> existsInCacheOrPersistent(
    final @NotNull String id
  ) {
    return CompletableFuture.supplyAsync(() -> this.existsInCacheOrPersistentSync(id), super.executor);
  }

  public @NotNull CompletableFuture<@NotNull Boolean> existsInBoth(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.existsInBothSync(id), super.executor);
  }

  public @NotNull CompletableFuture<@NotNull ModelType> saveInCache(final @NotNull ModelType model) {
    return CompletableFuture.supplyAsync(() -> this.saveInCacheSync(model), super.executor);
  }

  public @NotNull CompletableFuture<@NotNull ModelType> saveInBoth(final @NotNull ModelType model) {
    return CompletableFuture.supplyAsync(() -> this.saveInBothSync(model), super.executor);
  }

  public @NotNull CompletableFuture<Boolean> deleteInCache(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.deleteInCacheSync(id), super.executor);
  }

  public @NotNull CompletableFuture<Boolean> deleteInBoth(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.deleteInBothSync(id), super.executor);
  }

  public @NotNull CompletableFuture<Void> saveAll() {
    return this.saveAll(modelType -> { });
  }

  public @NotNull CompletableFuture<Void> saveAll(final @NotNull Consumer<ModelType> preSaveAction) {
    return CompletableFuture.runAsync(() -> this.saveAllSync(preSaveAction), super.executor);
  }
}
