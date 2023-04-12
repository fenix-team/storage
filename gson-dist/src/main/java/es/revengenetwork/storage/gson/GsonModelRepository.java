package es.revengenetwork.storage.gson;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.repository.AbstractAsyncModelRepository;
import es.revengenetwork.storage.repository.ModelRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class GsonModelRepository<ModelType extends Model, Reader extends ModelReader<JsonObject>>
  extends AbstractAsyncModelRepository<ModelType> {
  protected final Class<ModelType> modelType;
  protected final Path folderPath;
  protected final boolean prettyPrinting;
  protected final ModelCodec.Writer<ModelType, JsonObject> writer;
  protected final Function<JsonObject, Reader> readerFactory;
  protected final ModelCodec.Reader<ModelType, JsonObject, Reader> reader;

  protected GsonModelRepository(
    final @NotNull Executor executor,
    final @NotNull Class<ModelType> modelType,
    final @NotNull Path folderPath,
    final boolean prettyPrinting,
    final ModelCodec.@NotNull Writer<ModelType, JsonObject> writer,
    final @NotNull Function<JsonObject, Reader> readerFactory,
    final ModelCodec.@NotNull Reader<ModelType, JsonObject, Reader> reader
  ) {
    super(executor);
    this.prettyPrinting = prettyPrinting;
    this.modelType = modelType;
    this.folderPath = folderPath;
    this.writer = writer;
    this.readerFactory = readerFactory;
    this.reader = reader;
  }

  @Contract("_ -> new")
  public static <T extends Model, R extends ModelReader<JsonObject>> @NotNull GsonModelRepositoryBuilder<T, R> builder(
    final @NotNull Class<T> type
  ) {
    return new GsonModelRepositoryBuilder<>(type);
  }

  @Override
  public @Nullable ModelType findSync(final @NotNull String id) {
    return this.internalFind(this.resolveChild(id));
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
    final var model = this.findSync(value);
    if (model == null) {
      return null;
    }
    final var collection = factory.apply(1);
    collection.add(model);
    return collection;
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    try (final var walk = Files.walk(this.folderPath)) {
      return walk.filter(Files::isRegularFile)
               .map(Path::getFileName)
               .map(Path::toString)
               .map(fileName -> fileName.substring(0, fileName.length() - 5))
               .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    try (final var walk = Files.walk(this.folderPath, 1)) {
      final var foundModels = factory.apply(1);
      walk.filter(Files::isRegularFile)
        .map(this::internalFind)
        .filter(Objects::nonNull)
        .forEach(model -> {
          postLoadAction.accept(model);
          foundModels.add(model);
        });
      return foundModels;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean existsSync(final @NotNull String id) {
    return Files.exists(this.resolveChild(id));
  }

  @Override
  public @NotNull ModelType saveSync(final @NotNull ModelType model) {
    final var modelPath = this.resolveChild(model.id());
    try {
      if (Files.notExists(modelPath)) {
        Files.createFile(modelPath);
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    try (final var writer = new JsonWriter(Files.newBufferedWriter(modelPath, StandardCharsets.UTF_8))) {
      writer.setSerializeNulls(false);
      if (this.prettyPrinting) {
        writer.setIndent("  ");
      }
      final var jsonObject = this.writer.serialize(model);
      TypeAdapters.JSON_ELEMENT.write(writer, jsonObject);
      return model;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean deleteSync(final @NotNull String id) {
    try {
      return Files.deleteIfExists(this.resolveChild(id));
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected @NotNull Path resolveChild(final @NotNull String id) {
    return this.folderPath.resolve(id + ".json");
  }

  protected @Nullable ModelType internalFind(final @NotNull Path file) {
    if (Files.notExists(file)) {
      return null;
    }
    try (final var reader = new JsonReader(Files.newBufferedReader(file))) {
      final var jsonObject = new JsonObject();
      reader.beginObject();
      while (reader.hasNext()) {
        jsonObject.add(reader.nextName(), TypeAdapters.JSON_ELEMENT.read(reader));
      }
      reader.endObject();
      return this.reader.deserialize(this.readerFactory.apply(jsonObject));
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
