package es.revengenetwork.storage.gson.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class JsonReader implements ModelReader<JsonObject> {
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

  protected final JsonObject jsonObject;

  protected JsonReader(final @NotNull JsonObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  @Contract("_ -> new")
  public static @NotNull JsonReader create(final @NotNull JsonObject jsonObject) {
    return new JsonReader(jsonObject);
  }

  @Override
  public @NotNull JsonObject raw() {
    return this.jsonObject;
  }

  @Override
  public @Nullable JsonObject readThis(final @NotNull String field) {
    return this.jsonObject.getAsJsonObject(field);
  }

  @Override
  public @Nullable String readString(final @NotNull String field) {
    final var element = this.jsonObject.get(field);
    if (element == null) {
      return null;
    }
    return element.getAsString();
  }

  @Override
  public @Nullable Number readNumber(final @NotNull String field) {
    final var element = this.jsonObject.get(field);
    if (element == null) {
      return null;
    }
    return element.getAsNumber();
  }

  @Override
  public boolean readBoolean(final @NotNull String field) {
    final var element = this.jsonObject.get(field);
    if (element == null) {
      return false;
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
    final var element = this.jsonObject.get(field);
    if (element == null) {
      return null;
    }
    final var array = element.getAsJsonArray();
    final var collection = collectionFactory.apply(array.size());
    final Function<JsonElement, Object> reader = READERS.get(clazz);
    for (final JsonElement jsonElement : array) {
      collection.add((T) reader.apply(jsonElement));
    }
    return collection;
  }

  @Override
  public <T, R extends ModelReader<JsonObject>> @Nullable T readObject(
    final @NotNull String field,
    final @NotNull Function<JsonObject, R> readerFactory,
    final ModelCodec.@NotNull Reader<T, JsonObject, R> reader
  ) {
    final JsonElement element = this.jsonObject.get(field);
    if (element == null) {
      return null;
    }
    return reader.deserialize(readerFactory.apply(element.getAsJsonObject()));
  }

  @Override
  public @Nullable <K, V, R extends ModelReader<JsonObject>> Map<K, V> readMap(
    final @NotNull String field,
    final @NotNull Function<V, K> keyParser,
    final @NotNull Function<JsonObject, R> readerFactory,
    final ModelCodec.@NotNull Reader<V, JsonObject, R> reader
  ) {
    final var element = this.jsonObject.get(field);
    if (element == null) {
      return null;
    }
    final var array = element.getAsJsonArray();
    final var map = new HashMap<K, V>(array.size());
    for (final var arrayElement : array) {
      final var value = reader.deserialize(readerFactory.apply(arrayElement.getAsJsonObject()));
      map.put(keyParser.apply(value), value);
    }
    return map;
  }

  @Override
  public <T, C extends Collection<T>, R extends ModelReader<JsonObject>> @Nullable C readCollection(
    final @NotNull String field,
    final ModelCodec.@NotNull Reader<T, JsonObject, R> reader,
    final @NotNull Function<JsonObject, R> readerFactory,
    final @NotNull Function<Integer, C> collectionFactory
  ) {
    final var array = this.jsonObject.getAsJsonArray(field);
    if (array == null) {
      return null;
    }
    final var objects = collectionFactory.apply(array.size());
    for (final var element : array) {
      final var object = reader.deserialize(readerFactory.apply(element.getAsJsonObject()));
      objects.add(object);
    }
    return objects;
  }

  public <T, C extends Collection<T>> @Nullable C readPrimitiveCollection(
    final @NotNull String field,
    final @NotNull Function<JsonElement, T> reader,
    final @NotNull Function<Integer, C> collectionFactory
  ) {
    final var array = this.jsonObject.getAsJsonArray(field);
    if (array == null) {
      return null;
    }
    final var objects = collectionFactory.apply(array.size());
    for (final var element : array) {
      final var object = reader.apply(element);
      objects.add(object);
    }
    return objects;
  }

  public @Nullable UUID readDetailedUuid(final @NotNull String field) {
    final var element = this.jsonObject.get(field);
    if (!(element instanceof JsonObject serializedUuid)) {
      return null;
    }
    return this.readDetailedUuid(serializedUuid);
  }

  public @Nullable <C extends Collection<UUID>> C readDetailedUuids(
    final @NotNull String field,
    final @NotNull Function<Integer, C> factory
  ) {
    final var array = this.jsonObject.getAsJsonArray(field);
    if (array == null) {
      return null;
    }
    final C uuids = factory.apply(array.size());
    for (final var element : array) {
      if (!(element instanceof JsonObject serializedUuid)) {
        continue;
      }
      final var uuid = this.readDetailedUuid(serializedUuid);
      uuids.add(uuid);
    }
    return uuids;
  }

  public @Nullable UUID readDetailedUuid(final @NotNull JsonObject serializedUuid) {
    final var mostSignificantBitsElement = serializedUuid.get("most");
    final var leastSignificantBitsElement = serializedUuid.get("least");
    if (mostSignificantBitsElement == null || leastSignificantBitsElement == null) {
      return null;
    }
    return new UUID(mostSignificantBitsElement.getAsLong(), leastSignificantBitsElement.getAsLong());
  }
}
