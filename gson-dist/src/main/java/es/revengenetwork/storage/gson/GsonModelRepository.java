package es.revengenetwork.storage.gson;

import com.google.gson.Gson;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.repository.AbstractAsyncModelRepository;
import es.revengenetwork.storage.repository.ModelRepository;
import org.jetbrains.annotations.Contract;
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
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class GsonModelRepository<ModelType extends Model>
  extends AbstractAsyncModelRepository<ModelType> {

  @Contract("_ -> new")
  public static <T extends Model> @NotNull GsonModelRepositoryBuilder<T> builder(
    final @NotNull Class<T> type
  ) {
    return new GsonModelRepositoryBuilder<>(type);
  }

  private final Gson gson;
  private final Class<ModelType> modelType;
  private final Path folderPath;

  protected GsonModelRepository(
    final @NotNull Executor executor,
    final @NotNull Gson gson,
    final @NotNull Class<ModelType> modelType,
    final @NotNull Path folderPath
  ) {
    super(executor);
    this.gson = gson;
    this.modelType = modelType;
    this.folderPath = folderPath;
  }

  @Override
  public @Nullable ModelType findSync(final @NotNull String id) {
    return internalFind(resolveChild(id));
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findSync(
    final @NotNull String field,
    final @NotNull String value,
    final @NotNull Function<Integer, C> factory
  ) {
    if (!field.equals(ModelRepository.ID_FIELD)) {
      throw new IllegalArgumentException("Only ID field is supported for JSON find");
    }

    final ModelType model = this.findSync(value);

    if (model == null) {
      return null;
    }

    final C collection = factory.apply(1);
    collection.add(model);
    return collection;
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
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    try (Stream<Path> walk = Files.walk(this.folderPath, 1)) {
      final C foundModels = factory.apply(1);

      walk.filter(Files::isRegularFile)
        .map(this::internalFind)
        .filter(Objects::nonNull)
        .forEach(model -> {
          postLoadAction.accept(model);
          foundModels.add(model);
        });

      return foundModels;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean existsSync(final @NotNull String id) {
    return Files.exists(this.resolveChild(id));
  }

  @Override
  public void saveSync(final @NotNull ModelType model) {
    final Path modelPath = resolveChild(model.getId());

    try {
      if (Files.notExists(modelPath)) {
        Files.createFile(modelPath);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try (final Writer writer = Files.newBufferedWriter(modelPath, StandardCharsets.UTF_8)) {
      this.gson.toJson(model, this.modelType, writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean deleteSync(final @NotNull String id) {
    try {
      return Files.deleteIfExists(this.resolveChild(id));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private @NotNull Path resolveChild(final @NotNull String id) {
    return this.folderPath.resolve(id + ".json");
  }

  private @Nullable ModelType internalFind(final @NotNull Path file) {
    if (Files.notExists(file)) {
      return null;
    }

    try (final Reader reader = Files.newBufferedReader(file)) {
      return this.gson.fromJson(reader, this.modelType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
