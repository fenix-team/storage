package es.revengenetwork.storage.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CaffeineModelService<ModelType extends Model>
  implements ModelService<ModelType> {

  private final Cache<String, ModelType> cache;

  protected CaffeineModelService(Cache<String, ModelType> cache) {
    this.cache = cache;
  }

  public static <T extends Model> CaffeineModelService<T> create(Cache<String, T> cache) {
    return new CaffeineModelService<>(cache);
  }

  @Override
  public @Nullable ModelType findSync(@NotNull String id) {
    return cache.getIfPresent(id);
  }

  @Override
  public @Nullable List<ModelType> findSync(@NotNull String field, @NotNull String value) {
    return Collections.singletonList(findSync(value));
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    return cache.asMap().keySet();
  }

  @Override
  public @Nullable List<ModelType> findAllSync(@NotNull Consumer<ModelType> postLoadAction) {
    return cache.asMap()
             .values()
             .stream()
             .peek(postLoadAction)
             .toList();
  }

  @Override
  public boolean existsSync(@NotNull final String id) {
    return cache.asMap().containsKey(id);
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
