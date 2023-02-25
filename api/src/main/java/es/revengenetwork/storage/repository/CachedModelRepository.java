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

  public @Nullable ModelType getSync(@NotNull final String id) {
    return this.cacheModelRepository.findSync(id);
  }

  public @Nullable ModelType getOrFindSync(@NotNull final String id) {
    final ModelType model = this.getSync(id);

    if (model != null) {
      return model;
    }

    return this.findSync(id);
  }

  public @Nullable ModelType getOrFindAndCacheSync(@NotNull final String id) {
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
    @NotNull final Function<Integer, C> factory
  ) {
    return this.cacheModelRepository.findAllSync(factory);
  }

  public <C extends Collection<ModelType>> @Nullable C loadAllSync(
    @NotNull final Consumer<ModelType> postLoadAction,
    @NotNull final Function<Integer, C> factory
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

  public void uploadSync(@NotNull final ModelType model) {
    this.cacheModelRepository.deleteSync(model);
    this.persistModelRepository.saveSync(model);
  }

  public void uploadAllSync(@NotNull final Consumer<ModelType> preUploadAction) {
    this.persistModelRepository.findAllSync(
      model -> {
        preUploadAction.accept(model);
        this.cacheModelRepository.deleteSync(model);
        this.persistModelRepository.saveSync(model);
      },
      ArrayList::new
    );
  }

  public boolean existsInCacheSync(@NotNull final String id) {
    return this.cacheModelRepository.existsSync(id);
  }

  public boolean existsInCacheOrPersistentSync(@NotNull final String id) {
    return this.existsInCacheSync(id) || this.persistModelRepository.existsSync(id);
  }

  public boolean existsInBothSync(@NotNull final String id) {
    return this.existsInCacheSync(id) && this.persistModelRepository.existsSync(id);
  }

  public void saveInCacheSync(@NotNull final ModelType model) {
    this.cacheModelRepository.saveSync(model);
  }

  public void saveInBothSync(@NotNull final ModelType model) {
    this.cacheModelRepository.saveSync(model);
    this.persistModelRepository.saveSync(model);
  }

  public boolean deleteInCacheSync(@NotNull final String id) {
    return this.cacheModelRepository.deleteSync(id);
  }

  public boolean deleteInBothSync(@NotNull final String id) {
    return this.cacheModelRepository.deleteSync(id) &&
           this.persistModelRepository.deleteSync(id);
  }

  public void saveAllSync(@NotNull final Consumer<ModelType> preSaveAction) {
    this.cacheModelRepository.findAllSync(
      model -> {
        preSaveAction.accept(model);
        this.persistModelRepository.saveSync(model);
      },
      ArrayList::new
    );
  }

  @Override
  public @Nullable ModelType findSync(@NotNull final String id) {
    return this.persistModelRepository.findSync(id);
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findSync(
    @NotNull final String field,
    @NotNull final String value,
    @NotNull final Function<Integer, C> factory
  ) {
    return this.persistModelRepository.findSync(field, value, factory);
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    return this.persistModelRepository.findIdsSync();
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    @NotNull final Consumer<ModelType> postLoadAction,
    @NotNull final Function<Integer, C> factory
  ) {
    return this.persistModelRepository.findAllSync(postLoadAction, factory);
  }

  @Override
  public boolean existsSync(@NotNull final String id) {
    return this.persistModelRepository.existsSync(id);
  }

  @Override
  public void saveSync(@NotNull final ModelType model) {
    this.persistModelRepository.saveSync(model);
  }

  @Override
  public boolean deleteSync(@NotNull final String id) {
    return this.persistModelRepository.deleteSync(id);
  }

  public @NotNull CompletableFuture<@Nullable ModelType> get(@NotNull String id) {
    return CompletableFuture.supplyAsync(() -> getSync(id), executor);
  }

  public @NotNull CompletableFuture<@Nullable ModelType> getOrFind(@NotNull String id) {
    return CompletableFuture.supplyAsync(() -> getOrFindSync(id), executor);
  }

  public @NotNull CompletableFuture<@Nullable ModelType> getOrFindAndCache(
    @NotNull final String id
  ) {
    return CompletableFuture.supplyAsync(() -> getOrFindAndCacheSync(id), executor);
  }

  public @NotNull CompletableFuture<@Nullable Collection<String>> getAllIds() {
    return CompletableFuture.supplyAsync(this::getAllIdsSync, executor);
  }

  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> getAll(
    @NotNull final Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> getAllSync(factory), executor);
  }

  public @NotNull <C extends Collection<ModelType>> CompletableFuture<@Nullable C> loadAll(
    final @NotNull Consumer<ModelType> postLoadAction,
    @NotNull final Function<Integer, C> factory
  ) {
    return CompletableFuture.supplyAsync(() -> loadAllSync(postLoadAction, factory), executor);
  }

  public @NotNull CompletableFuture<Void> upload(@NotNull ModelType model) {
    return CompletableFuture.runAsync(() -> uploadSync(model), executor);
  }

  public @NotNull CompletableFuture<Void> uploadAll() {
    return uploadAll(modelType -> { });
  }

  public @NotNull CompletableFuture<Void> uploadAll(@NotNull Consumer<ModelType> preUploadAction) {
    return CompletableFuture.runAsync(() -> uploadAllSync(preUploadAction), executor);
  }

  public @NotNull CompletableFuture<@NotNull Boolean> existsInCache(@NotNull final String id) {
    return CompletableFuture.supplyAsync(() -> existsInCacheSync(id), executor);
  }

  public @NotNull CompletableFuture<@NotNull Boolean> existsInCacheOrPersistent(
    @NotNull final String id
  ) {
    return CompletableFuture.supplyAsync(() -> existsInCacheOrPersistentSync(id), executor);
  }

  public @NotNull CompletableFuture<@NotNull Boolean> existsInBoth(@NotNull final String id) {
    return CompletableFuture.supplyAsync(() -> existsInBothSync(id), executor);
  }

  public @NotNull CompletableFuture<Void> saveInCache(@NotNull final ModelType model) {
    return CompletableFuture.runAsync(() -> saveInCacheSync(model), executor);
  }

  public @NotNull CompletableFuture<Void> saveInBoth(@NotNull final ModelType model) {
    return CompletableFuture.runAsync(() -> saveInBothSync(model), executor);
  }

  public @NotNull CompletableFuture<Boolean> deleteInCache(@NotNull final String id) {
    return CompletableFuture.supplyAsync(() -> deleteInCacheSync(id), executor);
  }

  public @NotNull CompletableFuture<Boolean> deleteInBoth(@NotNull final String id) {
    return CompletableFuture.supplyAsync(() -> deleteInBothSync(id), executor);
  }

  public @NotNull CompletableFuture<Void> saveAll() {
    return saveAll(modelType -> { });
  }

  public @NotNull CompletableFuture<Void> saveAll(@NotNull Consumer<ModelType> preSaveAction) {
    return CompletableFuture.runAsync(() -> saveAllSync(preSaveAction), executor);
  }
}
