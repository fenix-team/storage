package es.revengenetwork.storage.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.repository.ModelRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class CaffeineModelRepository<ModelType extends Model>
  implements ModelRepository<ModelType> {

  private final Cache<String, ModelType> cache;

  protected CaffeineModelRepository(final @NotNull Cache<String, ModelType> cache) {
    this.cache = cache;
  }

  public static <T extends Model> @NotNull CaffeineModelRepository<T> create(
    final @NotNull Cache<String, T> cache
  ) {
    return new CaffeineModelRepository<>(cache);
  }

  @Override
  public @Nullable ModelType findSync(@NotNull String id) {
    return cache.getIfPresent(id);
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findSync(
    final @NotNull String field,
    final @NotNull String value,
    final @NotNull Function<Integer, C> factory
  ) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    return cache.asMap()
             .keySet();
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    final Collection<ModelType> values = cache.asMap()
                                           .values();
    final C foundModels = factory.apply(values.size());

    for (final ModelType value : values) {
      postLoadAction.accept(value);
      foundModels.add(value);
    }

    return foundModels;
  }

  @Override
  public boolean existsSync(final @NotNull String id) {
    return cache.asMap()
             .containsKey(id);
  }

  @Override
  public void saveSync(@NotNull ModelType model) {
    cache.put(model.getId(), model);
  }

  @Override
  public boolean deleteSync(@NotNull String id) {
    cache.invalidate(id);
    return true;
  }
}
