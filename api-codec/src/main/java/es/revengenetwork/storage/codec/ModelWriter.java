package es.revengenetwork.storage.codec;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface ModelWriter<This extends ModelWriter<This, WriteType>, WriteType> {
  @Contract("_, _ -> this")
  @NotNull This writeThis(final @NotNull String key, final @Nullable WriteType value);

  @Contract("_, _ -> this")
  @NotNull This writeUuid(final @NotNull String field, final @Nullable UUID uuid);

  @Contract("_, _ -> this")
  default @NotNull This writeDate(final @NotNull String field, final @Nullable Date date) {
    if (date == null) {
      return this.writeNumber(field, null);
    }
    return this.writeNumber(field, date.getTime());
  }

  @Contract("_, _ -> this")
  @NotNull This writeString(final @NotNull String field, final @Nullable String value);

  @Contract("_, _ -> this")
  @NotNull This writeNumber(final @NotNull String field, final @Nullable Number value);

  @Contract("_, _ -> this")
  @NotNull This writeBoolean(final @NotNull String field, final @Nullable Boolean value);

  @Contract("_, _, _ -> this")
  <T> @NotNull This writeObject(
    final @NotNull String field,
    final @Nullable T child,
    final ModelCodec.@NotNull Writer<T, WriteType> writer
  );

  @Contract("_, _ -> this")
  @NotNull <T> This writeRawCollection(
    final @NotNull String field,
    final @Nullable Collection<T> children
  );

  @Contract("_, _, _ -> this")
  @NotNull <T> This writeCollection(
    final @NotNull String field,
    final @Nullable Collection<T> children,
    final ModelCodec.@NotNull Writer<T, WriteType> writer
  );

  @Contract("_, _, _ -> this")
  default <T> @NotNull This writeMap(
    final @NotNull String field,
    final @Nullable Map<?, T> children,
    final ModelCodec.@NotNull Writer<T, WriteType> writer
  ) {
    if (children == null) {
      return this.writeCollection(field, null, writer);
    }
    return this.writeCollection(field, children.values(), writer);
  }

  @Contract("_, _ -> this")
  @NotNull This writeObject(final @NotNull String field, final @Nullable Object value);

  @NotNull WriteType current();

  @NotNull WriteType end();
}
