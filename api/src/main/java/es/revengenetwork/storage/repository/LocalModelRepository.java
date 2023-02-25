package es.revengenetwork.storage.repository;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class LocalModelRepository<ModelType extends Model>
  implements ModelRepository<ModelType> {

  private final Map<String, ModelType> cache;

  private LocalModelRepository(final @NotNull Map<String, ModelType> cache) {
    this.cache = cache;
  }

  @Override
  public @Nullable ModelType findSync(final @NotNull String id) {
    return this.cache.get(id);
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findSync(
    final @NotNull String field,
    final @NotNull String value,
    final @NotNull Function<Integer, C> factory
  ) {
    throw new UnsupportedOperationException(
      "Local repository does not support findSync(String, String, Function)");
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    return this.cache.keySet();
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    final Collection<ModelType> values = this.cache.values();
    final C collection = factory.apply(values.size());

    for (final ModelType value : values) {
      postLoadAction.accept(value);
      collection.add(value);
    }

    return collection;
  }

  @Override
  public boolean existsSync(final @NotNull String id) {
    return this.cache.containsKey(id);
  }

  @Override
  public void saveSync(final @NotNull ModelType model) {
    this.cache.put(model.getId(), model);
  }

  @Override
  public boolean deleteSync(final @NotNull String id) {
    return this.cache.remove(id) != null;
  }

  public static <T extends Model> @NotNull LocalModelRepository<T> hashMap() {
    return LocalModelRepository.create(new HashMap<>());
  }

  public static <T extends Model> @NotNull LocalModelRepository<T> concurrent() {
    return LocalModelRepository.create(new ConcurrentHashMap<>());
  }

  public static <T extends Model> @NotNull LocalModelRepository<T> create(final @NotNull Map<String, T> cache) {
    return new LocalModelRepository<>(cache);
  }
}
