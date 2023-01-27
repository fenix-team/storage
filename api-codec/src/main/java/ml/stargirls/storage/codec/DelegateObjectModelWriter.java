package ml.stargirls.storage.codec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class DelegateObjectModelWriter<This extends ModelWriter<This, ReadType>, ReadType>
		implements ModelWriter<This, ReadType> {

	@Override
	public This writeThis(@NotNull String key, @Nullable ReadType value) {
		return writeObject(key, value);
	}

	@Override
	public This writeUuid(@NotNull String field, @Nullable UUID uuid) {
		if (uuid == null) {
			return writeString(field, null);
		}

		return writeObject(field, uuid.toString());
	}

	@Override
	public This writeString(@NotNull String field, @Nullable String value) {
		return writeObject(field, value);
	}

	@Override
	public This writeNumber(@NotNull String field, @Nullable Number value) {
		return writeObject(field, value);
	}

	@Override
	public This writeBoolean(@NotNull String field, @Nullable Boolean value) {
		return writeObject(field, value);
	}

	@Override
	public <T> This writeObject(
			@NotNull String field, @Nullable T child,
			@NotNull ModelCodec.Writer<T, ReadType> writer
	) {
		if (child == null) {
			return writeObject(field, null);
		}

		return writeObject(field, writer.serialize(child));
	}

	@Override
	public <T> This writeRawCollection(@NotNull final String field, @Nullable final Collection<T> children) {
		return writeObject(field, children);
	}

	@Override
	public <T> This writeCollection(
			@NotNull String field, @Nullable Collection<T> children,
			@NotNull ModelCodec.Writer<T, ReadType> writer
	) {
		if (children == null) {
			return writeObject(field, null);
		}

		List<ReadType> documents = new ArrayList<>(children.size());
		for (T child : children) {
			documents.add(writer.serialize(child));
		}

		return writeObject(field, documents);
	}
}
