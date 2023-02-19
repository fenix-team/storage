package es.revengenetwork.storage.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CaffeineModelService<T extends Model>
  implements ModelService<T> {

  private final Cache<String, T> cache;

  protected CaffeineModelService(Cache<String, T> cache) {
    this.cache = cache;
  }

  public static <T extends Model> CaffeineModelService<T> create(Cache<String, T> cache) {
    return new CaffeineModelService<>(cache);
  }

  @Override
  public @Nullable T findSync(@NotNull String id) {
    return cache.getIfPresent(id);
  }

  @Override
  public @Nullable List<T> findSync(@NotNull String field, @NotNull String value) {
    return Collections.singletonList(findSync(value));
  }

  @Override
  public @Nullable List<T> findAllSync(@NotNull Consumer<T> postLoadAction) {
    return cache.asMap()
             .values()
             .stream()
             .peek(postLoadAction)
             .toList();
  }

  @Override
  public void saveSync(@NotNull T model) {
    cache.put(model.getId(), model);
  }

  @Override
  public boolean deleteSync(@NotNull String id) {
    cache.invalidate(id);
    return true;
  }
}
