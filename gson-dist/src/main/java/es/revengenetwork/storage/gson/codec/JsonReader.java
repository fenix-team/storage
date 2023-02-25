package es.revengenetwork.storage.gson.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("unused")
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

  private JsonReader(final @NotNull JsonObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  @Contract("_ -> new")
  public static @NotNull JsonReader create(final @NotNull JsonObject jsonObject) {
    return new JsonReader(jsonObject);
  }

  @Override
  public @NotNull JsonObject getRaw() {
    return this.jsonObject;
  }

  @Override
  public @Nullable JsonObject readThis(final @NotNull String field) {
    return this.jsonObject.getAsJsonObject(field);
  }

  @Override
  public @Nullable String readString(final @NotNull String field) {
    final JsonElement element = this.jsonObject.get(field);

    if (element == null) {
      return null;
    }

    return element.getAsString();
  }

  @Override
  public @Nullable Number readNumber(final @NotNull String field) {
    final JsonElement element = this.jsonObject.get(field);

    if (element == null) {
      return null;
    }

    return element.getAsNumber();
  }

  @Override
  public @Nullable Boolean readBoolean(final @NotNull String field) {
    final JsonElement element = this.jsonObject.get(field);

    if (element == null) {
      return null;
    }

    return element.getAsBoolean();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T, C extends Collection<T>> @Nullable C readRawCollection(
    final @NotNull String field,
    final @NotNull Class<T> clazz,
    final @NotNull Function<Integer, C> collectionFactory
  ) {
    final JsonElement element = this.jsonObject.get(field);

    if (element == null) {
      return null;
    }

    final JsonArray array = element.getAsJsonArray();
    final C collection = collectionFactory.apply(array.size());

    final Function<JsonElement, Object> reader = READERS.get(clazz);

    for (final JsonElement jsonElement : array) {
      collection.add((T) reader.apply(jsonElement));
    }

    return collection;
  }

  @Override
  public <T> @Nullable T readObject(
    final @NotNull String field,
    final @NotNull ModelCodec.Reader<T, JsonObject, JsonReader> reader
  ) {
    final JsonElement element = this.jsonObject.get(field);

    if (element == null) {
      return null;
    }

    return reader.deserialize(JsonReader.create(element.getAsJsonObject()));
  }

  @Override
  public @Nullable <K, V> Map<K, V> readMap(
    final @NotNull String field,
    final @NotNull Function<V, K> keyParser,
    @NotNull ModelCodec.Reader<V, JsonObject, JsonReader> reader
  ) {
    final JsonElement element = this.jsonObject.get(field);

    if (element == null) {
      return null;
    }

    final JsonArray array = element.getAsJsonArray();
    final Map<K, V> map = new HashMap<>(array.size());

    for (final JsonElement arrayElement : array) {
      final V value = reader.deserialize(JsonReader.create(arrayElement.getAsJsonObject()));
      map.put(keyParser.apply(value), value);
    }

    return map;
  }

  @Override
  public <T, C extends Collection<T>> @Nullable C readCollection(
    final @NotNull String field,
    final @NotNull ModelCodec.Reader<T, JsonObject, JsonReader> reader,
    final @NotNull Function<Integer, C> collectionFactory
  ) {
    final JsonArray array = this.jsonObject.getAsJsonArray(field);

    if (array == null) {
      return null;
    }

    final C objects = collectionFactory.apply(array.size());

    for (final JsonElement element : array) {
      final T object = reader.deserialize(create(element.getAsJsonObject()));
      objects.add(object);
    }

    return objects;
  }

  public @Nullable UUID readDetailedUuid(final @NotNull String field) {
    final JsonElement element = this.jsonObject.get(field);

    if (!(element instanceof JsonObject serializedUuid)) {
      return null;
    }

    return this.readDetailedUuid(serializedUuid);
  }

  public @Nullable <C extends Collection<UUID>> C readDetailedUuids(
    final @NotNull String field,
    final @NotNull Function<Integer, C> factory
  ) {
    final JsonArray array = this.jsonObject.getAsJsonArray(field);

    if (array == null) {
      return null;
    }

    final C uuids = factory.apply(array.size());

    for (final JsonElement element : array) {
      if (!(element instanceof JsonObject serializedUuid)) {
        continue;
      }

      final UUID uuid = readDetailedUuid(serializedUuid);
      uuids.add(uuid);
    }

    return uuids;
  }

  public @Nullable UUID readDetailedUuid(final @NotNull JsonObject serializedUuid) {
    final JsonElement mostSignificantBitsElement = serializedUuid.get("most");
    final JsonElement leastSignificantBitsElement = serializedUuid.get("least");

    if (mostSignificantBitsElement == null || leastSignificantBitsElement == null) {
      return null;
    }

    return new UUID(
      mostSignificantBitsElement.getAsLong(),
      leastSignificantBitsElement.getAsLong());
  }
}
