package es.revengenetwork.storage.codec;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public abstract class AbstractObjectModelWriter<This extends ModelWriter<This, WriteType>, WriteType>
  implements ModelWriter<This, WriteType> {
  @Override
  @Contract("_, _ -> this")
  public @NotNull This writeThis(final @NotNull String key, final @Nullable WriteType value) {
    return this.writeObject(key, value);
  }

  @Override
  @Contract("_, _ -> this")
  public @NotNull This writeUuid(final @NotNull String field, final @Nullable UUID uuid) {
    if (uuid == null) {
      return this.writeString(field, null);
    }
    return this.writeObject(field, uuid.toString());
  }

  @Override
  @Contract("_, _ -> this")
  public @NotNull This writeString(final @NotNull String field, final @Nullable String value) {
    return this.writeObject(field, value);
  }

  @Override
  @Contract("_, _ -> this")
  public @NotNull This writeNumber(final @NotNull String field, final @Nullable Number value) {
    return this.writeObject(field, value);
  }

  @Override
  @Contract("_, _ -> this")
  public @NotNull This writeBoolean(final @NotNull String field, final @Nullable Boolean value) {
    return this.writeObject(field, value);
  }

  @Override
  @Contract("_, _, _ -> this")
  public <T> @NotNull This writeObject(
    final @NotNull String field,
    final @Nullable T child,
    final ModelCodec.@NotNull Writer<T, WriteType> writer
  ) {
    if (child == null) {
      return this.writeObject(field, null);
    }
    return this.writeObject(field, writer.serialize(child));
  }

  @Override
  @Contract("_, _ -> this")
  public <T> @NotNull This writeRawCollection(
    final @NotNull String field,
    final @Nullable Collection<T> children
  ) {
    return this.writeObject(field, children);
  }

  @Override
  @Contract("_, _, _ -> this")
  public <T> @NotNull This writeCollection(
    final @NotNull String field,
    final @Nullable Collection<T> children,
    final ModelCodec.@NotNull Writer<T, WriteType> writer
  ) {
    if (children == null) {
      return this.writeObject(field, null);
    }
    final var documents = new ArrayList<WriteType>(children.size());
    for (final var child : children) {
      documents.add(writer.serialize(child));
    }
    return this.writeObject(field, documents);
  }
}
