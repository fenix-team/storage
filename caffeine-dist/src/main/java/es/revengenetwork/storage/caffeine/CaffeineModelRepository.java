package es.revengenetwork.storage.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.repository.ModelRepository;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class CaffeineModelRepository<ModelType extends Model> implements ModelRepository<ModelType> {
  private final Cache<String, ModelType> cache;

  protected CaffeineModelRepository(final @NotNull Cache<String, ModelType> cache) {
    this.cache = cache;
  }

  @Contract(value = "_ -> new")
  public static <T extends Model> @NotNull CaffeineModelRepository<T> create(final @NotNull Cache<String, T> cache) {
    return new CaffeineModelRepository<>(cache);
  }

  @Override
  public @Nullable ModelType findSync(final @NotNull String id) {
    return this.cache.getIfPresent(id);
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
    return this.cache.asMap()
             .keySet();
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    final var values = this.cache.asMap()
                         .values();
    final var foundModels = factory.apply(values.size());
    for (final var value : values) {
      postLoadAction.accept(value);
      foundModels.add(value);
    }
    return foundModels;
  }

  @Override
  public boolean existsSync(final @NotNull String id) {
    return this.cache.asMap()
             .containsKey(id);
  }

  @Override
  public @NotNull ModelType saveSync(final @NotNull ModelType model) {
    this.cache.put(model.id(), model);
    return model;
  }

  @Override
  public boolean deleteSync(final @NotNull String id) {
    this.cache.invalidate(id);
    return true;
  }
}
