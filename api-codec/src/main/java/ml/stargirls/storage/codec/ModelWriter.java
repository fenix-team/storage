package ml.stargirls.storage.codec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public interface ModelWriter<This extends ModelWriter<This, ReadType>, ReadType> {

	This writeThis(@NotNull String key, @Nullable ReadType value);

	This writeUuid(@NotNull String field, @Nullable UUID uuid);

	default This writeDate(@NotNull String field, @Nullable Date date) {
		if (date == null) {
			return writeNumber(field, null);
		}

		return writeNumber(field, date.getTime());
	}

	This writeString(@NotNull String field, @Nullable String value);

	This writeNumber(@NotNull String field, @Nullable Number value);

	This writeBoolean(@NotNull String field, @Nullable Boolean value);

	<T> This writeObject(
			@NotNull String field, @Nullable T child,
			@NotNull ModelCodec.Writer<T, ReadType> writer
	);

	<T> This writeRawCollection(@NotNull String field, @Nullable Collection<T> children);

	<T> This writeCollection(
			@NotNull String field, @Nullable Collection<T> children,
			@NotNull ModelCodec.Writer<T, ReadType> writer
	);

	default <T> This writeMap(
			@NotNull String field, @Nullable Map<?, T> children,
			@NotNull ModelCodec.Writer<T, ReadType> writer
	) {
		if (children == null) {
			return writeCollection(field, null, writer);
		}

		return writeCollection(field, children.values(), writer);
	}

	This writeObject(@NotNull String field, @Nullable Object value);

	@NotNull ReadType current();

	@NotNull ReadType end();
}
