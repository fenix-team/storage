package es.revengenetwork.storage.dist;

import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LocalModelService<ModelType extends Model>
  implements ModelService<ModelType> {

  private final Map<String, ModelType> cache;

  private LocalModelService(@NotNull Map<String, ModelType> cache) {
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
  public List<ModelType> findAllSync(@NotNull Consumer<ModelType> postLoadAction) {
    return new ArrayList<>(cache.values());
  }

  @Override
  public void saveSync(@NotNull ModelType model) {
    cache.put(model.getId(), model);
  }

  @Override
  public void deleteSync(@NotNull ModelType model) {
    cache.remove(model.getId());
  }

  @Override
  public boolean deleteSync(@NotNull String id) {
    return cache.remove(id) != null;
  }

  public static <T extends Model> LocalModelService<T> hashMap() {
    return new LocalModelService<>(new HashMap<>());
  }

  public static <T extends Model> LocalModelService<T> concurrent() {
    return new LocalModelService<>(new ConcurrentHashMap<>());
  }

  public static <T extends Model> LocalModelService<T> create(Map<String, T> cache) {
    return new LocalModelService<>(cache);
  }
}
