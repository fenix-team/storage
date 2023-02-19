package es.revengenetwork.storage.gson.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class JsonReader
  implements ModelReader<JsonReader, JsonObject> {

  private static final Map<Class<?>, Function<JsonElement, Object>> READERS = new HashMap<>();

  static {
    READERS.put(String.class, JsonElement::getAsString);
    READERS.put(Integer.class, JsonElement::getAsInt);
    READERS.put(Long.class, JsonElement::getAsLong);
    READERS.put(Double.class, JsonElement::getAsDouble);
    READERS.put(Float.class, JsonElement::getAsFloat);
    READERS.put(Boolean.class, JsonElement::getAsBoolean);
    READERS.put(Byte.class, JsonElement::getAsByte);
    READERS.put(Short.class, JsonElement::getAsShort);
  }

  private final JsonObject jsonObject;

  private JsonReader(@NotNull JsonObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  public static JsonReader create(@NotNull JsonObject jsonObject) {
    return new JsonReader(jsonObject);
  }

  @Override
  public @NotNull JsonObject getRaw() {
    return jsonObject;
  }

  @Override
  public @Nullable JsonObject readThis(@NotNull String field) {
    return jsonObject.getAsJsonObject(field);
  }

  @Override
  public @Nullable String readString(@NotNull String field) {
    JsonElement element = jsonObject.get(field);

    if (element == null) {
      return null;
    }

    return element.getAsString();
  }

  @Override
  public @Nullable Number readNumber(@NotNull String field) {
    JsonElement element = jsonObject.get(field);

    if (element == null) {
      return null;
    }

    return element.getAsNumber();
  }

  @Override
  public @Nullable Boolean readBoolean(@NotNull String field) {
    JsonElement element = jsonObject.get(field);

    if (element == null) {
      return null;
    }

    return element.getAsBoolean();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T, C extends Collection<T>> @Nullable C readRawCollection(
    @NotNull String field, @NotNull Class<T> clazz,
    @NotNull Function<Integer, C> collectionFactory
  ) {
    JsonElement element = jsonObject.get(field);

    if (element == null) {
      return null;
    }

    JsonArray array = element.getAsJsonArray();
    C collection = collectionFactory.apply(array.size());

    Function<JsonElement, Object> reader = READERS.get(clazz);

    for (JsonElement jsonElement : array) {
      collection.add((T) reader.apply(jsonElement));
    }

    return collection;
  }

  @Override
  public <T> @Nullable T readObject(
    @NotNull String field,
    ModelCodec.@NotNull Reader<T, JsonObject, JsonReader> reader
  ) {
    JsonElement element = jsonObject.get(field);

    if (element == null) {
      return null;
    }

    return reader.deserialize(create(element.getAsJsonObject()));
  }

  @Override
  public @Nullable <K, V> Map<K, V> readMap(
    @NotNull String field, @NotNull Function<V, K> keyParser,
    ModelCodec.@NotNull Reader<V, JsonObject, JsonReader> reader
  ) {
    JsonElement element = jsonObject.get(field);

    if (element == null) {
      return null;
    }

    JsonArray array = element.getAsJsonArray();
    Map<K, V> map = new HashMap<>(array.size());

    for (JsonElement arrayElement : array) {
      V value = reader.deserialize(create(arrayElement.getAsJsonObject()));
      map.put(keyParser.apply(value), value);
    }

    return map;
  }

  @Override
  public <T, C extends Collection<T>> @Nullable C readCollection(
    @NotNull String field, ModelCodec.@NotNull Reader<T, JsonObject, JsonReader> reader,
    @NotNull Function<Integer, C> collectionFactory
  ) {
    JsonArray array = jsonObject.getAsJsonArray(field);

    if (array == null) {
      return null;
    }

    C objects = collectionFactory.apply(array.size());

    for (JsonElement element : array) {
      T object = reader.deserialize(create(element.getAsJsonObject()));
      objects.add(object);
    }

    return objects;
  }

  public @Nullable UUID readDetailedUuid(@NotNull String field) {
    JsonElement element = jsonObject.get(field);

    if (!(element instanceof JsonObject serializedUuid)) {
      return null;
    }

    return readDetailedUuid(serializedUuid);
  }

  public @Nullable <C extends Collection<UUID>> C readDetailedUuids(
    @NotNull String field,
    @NotNull Function<Integer, C> factory
  ) {
    JsonArray array = jsonObject.getAsJsonArray(field);

    if (array == null) {
      return null;
    }

    C uuids = factory.apply(array.size());

    for (JsonElement element : array) {
      if (!(element instanceof JsonObject serializedUuid)) {
        continue;
      }

      UUID uuid = readDetailedUuid(serializedUuid);
      uuids.add(uuid);
    }

    return uuids;
  }

  public @Nullable UUID readDetailedUuid(@NotNull JsonObject serializedUuid) {
    JsonElement mostSignificantBitsElement = serializedUuid.get("most");
    JsonElement leastSignificantBitsElement = serializedUuid.get("least");

    if (mostSignificantBitsElement == null || leastSignificantBitsElement == null) {
      return null;
    }

    return new UUID(
      mostSignificantBitsElement.getAsLong(),
      leastSignificantBitsElement.getAsLong());
  }
}
