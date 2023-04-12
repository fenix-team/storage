package es.revengenetwork.storage.codec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface ModelReader<ReadType> {
  @NotNull ReadType getRaw();

  @Nullable ReadType readThis(final @NotNull String field);

  default @Nullable UUID readUuid(final @NotNull String field) {
    final var uuidString = this.readString(field);
    if (uuidString == null) {
      return null;
    }
    return UUID.fromString(uuidString);
  }

  default @Nullable Date readDate(final @NotNull String field) {
    final var value = this.readNumber(field);
    if (value == null) {
      return null;
    }
    return new Date(value.longValue());
  }

  @Nullable String readString(final @NotNull String field);

  @Nullable Number readNumber(final @NotNull String field);

  default int readInt(final @NotNull String field) {
    final var value = this.readNumber(field);
    if (value == null) {
      return 0;
    }
    return value.intValue();
  }

  default long readLong(final @NotNull String field) {
    final var value = this.readNumber(field);
    if (value == null) {
      return 0;
    }
    return value.longValue();
  }

  default double readDouble(final @NotNull String field) {
    final var value = this.readNumber(field);
    if (value == null) {
      return 0;
    }
    return value.doubleValue();
  }

  default float readFloat(final @NotNull String field) {
    final var value = this.readNumber(field);
    if (value == null) {
      return 0;
    }
    return value.floatValue();
  }

  default short readShort(final @NotNull String field) {
    final var value = this.readNumber(field);
    if (value == null) {
      return 0;
    }
    return value.shortValue();
  }

  default byte readByte(final @NotNull String field) {
    final var value = this.readNumber(field);
    if (value == null) {
      return 0;
    }
    return value.byteValue();
  }

  boolean readBoolean(final @NotNull String field);

  <T, C extends Collection<T>> @Nullable C readRawCollection(
    final @NotNull String field,
    final @NotNull Class<T> clazz,
    final @NotNull Function<Integer, C> collectionFactory
  );

  <T, R extends ModelReader<ReadType>> @Nullable T readObject(
    final @NotNull String field,
    final @NotNull Function<ReadType, R> readerFactory,
    final ModelCodec.@NotNull Reader<T, ReadType, R> reader
  );

  <K, V, R extends ModelReader<ReadType>> @Nullable Map<K, V> readMap(
    final @NotNull String field,
    final @NotNull Function<V, K> keyParser,
    final @NotNull Function<ReadType, R> readerFactory,
    final ModelCodec.@NotNull Reader<V, ReadType, R> reader
  );

  <T, C extends Collection<T>, R extends ModelReader<ReadType>> @Nullable C readCollection(
    final @NotNull String field,
    final ModelCodec.@NotNull Reader<T, ReadType, R> reader,
    final @NotNull Function<ReadType, R> readerFactory,
    final @NotNull Function<Integer, C> collectionFactory
  );
}
