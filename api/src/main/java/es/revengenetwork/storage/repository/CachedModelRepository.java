package es.revengenetwork.storage.repository;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class CachedModelRepository<ModelType extends Model>
  extends AbstractAsyncModelRepository<ModelType> {

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

  public @Nullable ModelType getSync(final @NotNull String id) {
    return this.cacheModelRepository.findSync(id);
  }

  public @Nullable ModelType getOrFindSync(final @NotNull String id) {
    final ModelType model = this.getSync(id);

    if (model != null) {
      return model;
    }

    return this.findSync(id);
  }

  public @Nullable ModelType getOrFindAndCacheSync(final @NotNull String id) {
    final ModelType model = this.getSync(id);

    if (model != null) {
      return model;
    }

    final ModelType foundModel = this.findSync(id);

    if (foundModel == null) {
      return null;
    }

    this.cacheModelRepository.saveSync(foundModel);
    return foundModel;
  }

  public @Nullable Collection<String> getAllIdsSync() {
    return this.cacheModelRepository.findIdsSync();
  }

  public <C extends Collection<ModelType>> @Nullable C getAllSync(
    final @NotNull Function<Integer, C> factory
  ) {
    return this.cacheModelRepository.findAllSync(factory);
  }

  public <C extends Collection<ModelType>> @Nullable C loadAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    final C models = this.persistModelRepository.findAllSync(postLoadAction, factory);

    if (models == null) {
      return null;
    }

    for (final ModelType model : models) {
      this.cacheModelRepository.saveSync(model);
    }

    return models;
  }

  public void uploadSync(final @NotNull ModelType model) {
    this.cacheModelRepository.deleteSync(model);
    this.persistModelRepository.saveSync(model);
  }

  public void uploadAllSync(final @NotNull Consumer<ModelType> preUploadAction) {
    this.cacheModelRepository.findAllSync(
      model -> {
        preUploadAction.accept(model);
        this.cacheModelRepository.deleteSync(model);
        this.persistModelRepository.saveSync(model);
      },
      ArrayList::new
    );
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

  public void saveInCacheSync(final @NotNull ModelType model) {
    this.cacheModelRepository.saveSync(model);
  }

  public void saveInBothSync(final @NotNull ModelType model) {
    this.cacheModelRepository.saveSync(model);
    this.persistModelRepository.saveSync(model);
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
      ArrayList::new
    );
  }

  @Override
  public @Nullable ModelType findSync(final @NotNull String id) {
    return this.persistModelRepository.findSync(id);
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findSync(
    final @NotNull String field,
    final @NotNull String value,
    final @NotNull Function<Integer, C> factory
  ) {
    return this.persistModelRepository.findSync(field, value, factory);
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
  public void saveSync(final @NotNull ModelType model) {
    this.persistModelRepository.saveSync(model);
  }

  @Override
  public boolean deleteSync(final @NotNull String id) {
    return this.persistModelRepository.deleteSync(id);
  }

  public @NotNull CompletableFuture<@Nullable ModelType> get(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.getSync(id), executor);
  }

  public @NotNull CompletableFuture<@Nullable ModelType> getOrFind(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.getOrFindSync(id), executor);
  }

  public @NotNull CompletableFuture<@Nullable ModelType> getOrFindAndCache(
    final @NotNull String id
  ) {
    return CompletableFuture.supplyAsync(() -> this.getOrFindAndCacheSync(id), executor);
  }

  public @NotNull CompletableFuture<@Nullable Collection<String>> getAllIds() {
    return CompletableFuture.supplyAsync(this::getAllIdsSync, executor);
  }

  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> getAll(
    final @NotNull Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> this.getAllSync(factory), executor);
  }

  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> loadAll(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> this.loadAllSync(postLoadAction, factory), executor);
  }

  public @NotNull CompletableFuture<Void> upload(final @NotNull ModelType model) {
    return CompletableFuture.runAsync(() -> this.uploadSync(model), executor);
  }

  public @NotNull CompletableFuture<Void> uploadAll() {
    return this.uploadAll(modelType -> { });
  }

  public @NotNull CompletableFuture<Void> uploadAll(final @NotNull Consumer<ModelType> preUploadAction) {
    return CompletableFuture.runAsync(() -> this.uploadAllSync(preUploadAction), executor);
  }

  public @NotNull CompletableFuture<@NotNull Boolean> existsInCache(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.existsInCacheSync(id), executor);
  }

  public @NotNull CompletableFuture<@NotNull Boolean> existsInCacheOrPersistent(
    final @NotNull String id
  ) {
    return CompletableFuture.supplyAsync(() -> this.existsInCacheOrPersistentSync(id), executor);
  }

  public @NotNull CompletableFuture<@NotNull Boolean> existsInBoth(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.existsInBothSync(id), executor);
  }

  public @NotNull CompletableFuture<Void> saveInCache(final @NotNull ModelType model) {
    return CompletableFuture.runAsync(() -> this.saveInCacheSync(model), executor);
  }

  public @NotNull CompletableFuture<Void> saveInBoth(final @NotNull ModelType model) {
    return CompletableFuture.runAsync(() -> this.saveInBothSync(model), executor);
  }

  public @NotNull CompletableFuture<Boolean> deleteInCache(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.deleteInCacheSync(id), executor);
  }

  public @NotNull CompletableFuture<Boolean> deleteInBoth(final @NotNull String id) {
    return CompletableFuture.supplyAsync(() -> this.deleteInBothSync(id), executor);
  }

  public @NotNull CompletableFuture<Void> saveAll() {
    return this.saveAll(modelType -> { });
  }

  public @NotNull CompletableFuture<Void> saveAll(final @NotNull Consumer<ModelType> preSaveAction) {
    return CompletableFuture.runAsync(() -> this.saveAllSync(preSaveAction), executor);
  }
}
