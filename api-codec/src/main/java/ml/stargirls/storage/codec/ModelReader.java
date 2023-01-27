package ml.stargirls.storage.codec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public interface ModelReader<This extends ModelReader<This, ReadType>, ReadType> {

	@NotNull ReadType getRaw();

	@Nullable ReadType readThis(@NotNull String field);

	default @Nullable UUID readUuid(@NotNull String field) {
		String uuidString = readString(field);

		if (uuidString == null) {
			return null;
		}

		return UUID.fromString(uuidString);
	}

	default <T> @Nullable Set<T> readSet(@NotNull String field, @NotNull Class<T> clazz) {
		return readRawCollection(field, clazz, HashSet::new);
	}

	default @Nullable Date readDate(@NotNull String field) {
		Number value = readNumber(field);

		if (value == null) {
			return null;
		}

		return new Date(value.longValue());
	}

	@Nullable String readString(@NotNull String field);

	@Nullable Number readNumber(@NotNull String field);

	@Nullable Boolean readBoolean(@NotNull String field);

	<T, C extends Collection<T>> @Nullable C readRawCollection(
			@NotNull String field, @NotNull Class<T> clazz,
			@NotNull Function<Integer, C> collectionFactory
	);

	<T> @Nullable T readObject(
			@NotNull String field,
			@NotNull ModelCodec.Reader<T, ReadType, This> reader
	);

	<K, V> @Nullable Map<K, V> readMap(
			@NotNull String field,
			@NotNull Function<V, K> keyParser,
			@NotNull ModelCodec.Reader<V, ReadType, This> reader
	);

	<T, C extends Collection<T>> @Nullable C readCollection(
			@NotNull String field,
			@NotNull ModelCodec.Reader<T, ReadType, This> reader,
			@NotNull Function<Integer, C> collectionFactory
	);
}
