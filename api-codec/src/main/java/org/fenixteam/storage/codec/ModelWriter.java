package org.fenixteam.storage.codec;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface ModelWriter<WriteType> {
  @Contract("_, _ -> this")
  @NotNull ModelWriter<WriteType> writeThis(final @NotNull String key, final @Nullable WriteType value);

  @Contract("_, _ -> this")
  @NotNull ModelWriter<WriteType> writeDetailedUuid(final @NotNull String key, final @Nullable UUID uuid);

  @Contract("_, _ -> this")
  @NotNull ModelWriter<WriteType> writeDetailedUuids(
    final @NotNull String key,
    final @Nullable Collection<@NotNull UUID> uuids
  );

  @Contract("_, _ -> this")
  @NotNull ModelWriter<WriteType> writeUuid(final @NotNull String field, final @Nullable UUID uuid);

  @Contract("_, _ -> this")
  default @NotNull ModelWriter<WriteType> writeDate(final @NotNull String field, final @Nullable Date date) {
    if (date == null) {
      return this.writeNumber(field, null);
    }
    return this.writeNumber(field, date.getTime());
  }

  @Contract("_, _ -> this")
  @NotNull ModelWriter<WriteType> writeString(final @NotNull String field, final @Nullable String value);

  @Contract("_, _ -> this")
  @NotNull ModelWriter<WriteType> writeNumber(final @NotNull String field, final @Nullable Number value);

  @Contract("_, _ -> this")
  @NotNull ModelWriter<WriteType> writeBoolean(final @NotNull String field, final @Nullable Boolean value);

  @Contract("_, _, _ -> this")
  <T> @NotNull ModelWriter<WriteType> writeObject(
    final @NotNull String field,
    final @Nullable T child,
    final @NotNull ModelSerializer<T, WriteType> modelSerializer
  );

  @Contract("_, _ -> this")
  @NotNull <T> ModelWriter<WriteType> writeRawCollection(
    final @NotNull String field,
    final @Nullable Collection<T> children
  );

  @Contract("_, _, _ -> this")
  @NotNull <T> ModelWriter<WriteType> writeCollection(
    final @NotNull String field,
    final @Nullable Collection<T> children,
    final @NotNull ModelSerializer<T, WriteType> modelSerializer
  );

  @Contract("_, _, _ -> this")
  default <T> @NotNull ModelWriter<WriteType> writeMap(
    final @NotNull String field,
    final @Nullable Map<?, T> children,
    final @NotNull ModelSerializer<T, WriteType> modelSerializer
  ) {
    if (children == null) {
      return this.writeCollection(field, null, modelSerializer);
    }
    return this.writeCollection(field, children.values(), modelSerializer);
  }

  @NotNull WriteType current();

  @NotNull WriteType end();
}
