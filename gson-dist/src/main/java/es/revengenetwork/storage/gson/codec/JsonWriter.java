package es.revengenetwork.storage.gson.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("unused")
public class JsonWriter implements ModelWriter<JsonWriter, JsonObject> {
  private final JsonObject jsonObject;

  protected JsonWriter(final @NotNull JsonObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  @Contract(" -> new")
  public static @NotNull JsonWriter create() {
    return new JsonWriter(new JsonObject());
  }

  @Contract("_ -> new")
  public static @NotNull JsonWriter create(final @NotNull JsonObject jsonObject) {
    return new JsonWriter(jsonObject);
  }

  @Contract("_, _ -> this")
  public @NotNull JsonWriter writeDetailedUuid(
    final @NotNull String key,
    final @Nullable UUID uuid
  ) {
    if (uuid == null) {
      return this;
    }
    this.jsonObject.add(key, this.writeDetailedUuid(uuid));
    return this;
  }

  @Contract("_, _ -> this")
  public @NotNull JsonWriter writeDetailedUuids(
    final @NotNull String key,
    final @Nullable Collection<@NotNull UUID> uuids
  ) {
    if (uuids == null) {
      return this;
    }
    final var array = new JsonArray(uuids.size());
    for (final var uuid : uuids) {
      final var serializedUuid = this.writeDetailedUuid(uuid);
      if (serializedUuid == null) {
        continue;
      }
      array.add(serializedUuid);
    }
    this.jsonObject.add(key, array);
    return this;
  }

  public @Nullable JsonObject writeDetailedUuid(final @Nullable UUID uuid) {
    if (uuid == null) {
      return null;
    }
    final var serializedUuid = new JsonObject();
    serializedUuid.addProperty("least", uuid.getLeastSignificantBits());
    serializedUuid.addProperty("most", uuid.getMostSignificantBits());
    return serializedUuid;
  }

  @Override
  @Contract("_, _ -> this")
  public @NotNull JsonWriter writeThis(
    final @NotNull String key,
    final @Nullable JsonObject value
  ) {
    this.jsonObject.add(key, value);
    return this;
  }

  @Override
  @Contract("_, _ -> this")
  public @NotNull JsonWriter writeUuid(final @NotNull String field, final @Nullable UUID uuid) {
    if (uuid == null) {
      return this;
    }
    this.jsonObject.addProperty(field, uuid.toString());
    return this;
  }

  @Override
  @Contract("_, _ -> this")
  public @NotNull JsonWriter writeString(
    final @NotNull String field,
    final @Nullable String value
  ) {
    if (value == null) {
      return this;
    }
    this.jsonObject.addProperty(field, value);
    return this;
  }

  @Override
  @Contract("_, _ -> this")
  public @NotNull JsonWriter writeNumber(
    final @NotNull String field,
    final @Nullable Number value
  ) {
    if (value == null) {
      return this;
    }
    this.jsonObject.addProperty(field, value);
    return this;
  }

  @Override
  @Contract("_, _ -> this")
  public @NotNull JsonWriter writeBoolean(
    final @NotNull String field,
    final @Nullable Boolean value
  ) {
    if (value == null) {
      return this;
    }
    this.jsonObject.addProperty(field, value);
    return this;
  }

  @Override
  @Contract("_, _, _ -> this")
  public <T> @NotNull JsonWriter writeObject(
    final @NotNull String field,
    final @Nullable T child,
    final ModelCodec.@NotNull Writer<T, JsonObject> writer
  ) {
    if (child == null) {
      return this;
    }
    this.jsonObject.add(field, writer.serialize(child));
    return this;
  }

  @Override
  @Contract("_, _ -> this")
  public <T> @NotNull JsonWriter writeRawCollection(
    final @NotNull String field,
    final @Nullable Collection<T> children
  ) {
    if (children == null) {
      return this;
    }
    final var array = new JsonArray(children.size());
    for (final var child : children) {
      if (child == null) {
        continue;
      }
      array.add(child.toString());
    }
    this.jsonObject.add(field, array);
    return this;
  }

  @Override
  @Contract("_, _, _ -> this")
  public <T> @NotNull JsonWriter writeCollection(
    final @NotNull String field,
    final @Nullable Collection<T> children,
    final ModelCodec.@NotNull Writer<T, JsonObject> writer
  ) {
    if (children == null) {
      return this;
    }
    final var array = new JsonArray(children.size());
    for (final var child : children) {
      array.add(writer.serialize(child));
    }
    this.jsonObject.add(field, array);
    return this;
  }

  @Contract("_, _, _ -> this")
  public <T> @NotNull JsonWriter writePrimitiveCollection(
    final @NotNull String field,
    final @Nullable Collection<T> children,
    final @NotNull Function<T, JsonElement> writer
  ) {
    if (children == null) {
      return this;
    }
    final var array = new JsonArray(children.size());
    for (final var child : children) {
      array.add(writer.apply(child));
    }
    this.jsonObject.add(field, array);
    return this;
  }

  @Override
  @Contract("_, _ -> this")
  public @NotNull JsonWriter writeObject(final @NotNull String field, final Object value) {
    return this;
  }

  @Override
  public @NotNull JsonObject current() {
    return this.jsonObject;
  }

  @Override
  public @NotNull JsonObject end() {
    return this.jsonObject;
  }
}
