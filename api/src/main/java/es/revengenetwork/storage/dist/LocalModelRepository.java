package es.revengenetwork.storage.dist;

import es.revengenetwork.storage.ModelRepository;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LocalModelRepository<ModelType extends Model>
  implements ModelRepository<ModelType> {

  private final Map<String, ModelType> cache;

  private LocalModelRepository(@NotNull Map<String, ModelType> cache) {
    this.cache = cache;
  }

  @Override
  public @Nullable ModelType findSync(@NotNull String id) {
    return cache.get(id);
  }

  @Override
  public List<ModelType> findSync(@NotNull String field, @NotNull String value) {
    return Collections.singletonList(findSync(value));
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    return cache.keySet();
  }

  @Override
  public List<ModelType> findAllSync(@NotNull Consumer<ModelType> postLoadAction) {
    return new ArrayList<>(cache.values());
  }

  @Override
  public boolean existsSync(@NotNull final String id) {
    return cache.containsKey(id);
  }

  @Override
  public void saveSync(@NotNull ModelType model) {
    cache.put(model.getId(), model);
  }

  @Override
  public boolean deleteSync(@NotNull String id) {
    return cache.remove(id) != null;
  }

  public static <T extends Model> LocalModelRepository<T> hashMap() {
    return new LocalModelRepository<>(new HashMap<>());
  }

  public static <T extends Model> LocalModelRepository<T> concurrent() {
    return new LocalModelRepository<>(new ConcurrentHashMap<>());
  }

  public static <T extends Model> LocalModelRepository<T> create(Map<String, T> cache) {
    return new LocalModelRepository<>(cache);
  }
}
