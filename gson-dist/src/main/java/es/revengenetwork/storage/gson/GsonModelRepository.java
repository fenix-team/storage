package es.revengenetwork.storage.gson;

import com.google.gson.Gson;
import es.revengenetwork.storage.ModelRepository;
import es.revengenetwork.storage.dist.RemoteModelRepository;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GsonModelRepository<ModelType extends Model>
  extends RemoteModelRepository<ModelType> {

  public static <T extends Model> GsonModelRepositoryBuilder<T> builder(Class<T> type) {
    return new GsonModelRepositoryBuilder<>(type);
  }

  private final Gson gson;
  private final Class<ModelType> modelType;
  private final Path folderPath;

  protected GsonModelRepository(
    @NotNull Executor executor,
    @NotNull Gson gson,
    @NotNull Class<ModelType> modelType,
    @NotNull Path folderPath
  ) {
    super(executor);
    this.gson = gson;
    this.modelType = modelType;
    this.folderPath = folderPath;
  }

  @Override
  public @Nullable ModelType findSync(@NotNull String id) {
    return internalFind(resolveChild(id));
  }

  @Override
  public List<ModelType> findSync(@NotNull String field, @NotNull String value) {
    if (!field.equals(ModelRepository.ID_FIELD)) {
      throw new IllegalArgumentException("Only ID field is supported for sync find");
    }

    return Collections.singletonList(findSync(value));
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    try (Stream<Path> walk = Files.walk(this.folderPath)) {
      return walk.filter(Files::isRegularFile)
               .map(Path::getFileName)
               .map(Path::toString)
               .map(fileName -> fileName.substring(0, fileName.length() - 5))
               .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<ModelType> findAllSync(@NotNull Consumer<ModelType> postLoadAction) {
    try (Stream<Path> walk = Files.walk(this.folderPath)) {
      return walk.filter(Files::isRegularFile)
               .map(this::internalFind)
               .filter(Objects::nonNull)
               .collect(ArrayList::new, (list, model) -> {
                 postLoadAction.accept(model);
                 list.add(model);
               }, ArrayList::addAll);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean existsSync(@NotNull final String id) {
    return Files.exists(this.resolveChild(id));
  }

  @Override
  public void saveSync(@NotNull ModelType model) {
    final Path modelPath = resolveChild(model.getId());

    try {
      if (Files.notExists(modelPath)) {
        Files.createFile(modelPath);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try (Writer writer = Files.newBufferedWriter(modelPath, StandardCharsets.UTF_8)) {
      this.gson.toJson(model, this.modelType, writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean deleteSync(@NotNull String id) {
    try {
      return Files.deleteIfExists(this.resolveChild(id));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private @NotNull Path resolveChild(String id) {
    return this.folderPath.resolve(id + ".json");
  }

  private @Nullable ModelType internalFind(final @NotNull Path file) {
    if (Files.notExists(file)) {
      return null;
    }

    try (Reader reader = Files.newBufferedReader(file)) {
      return this.gson.fromJson(reader, this.modelType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
