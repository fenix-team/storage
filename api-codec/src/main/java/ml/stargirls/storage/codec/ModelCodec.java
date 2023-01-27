package ml.stargirls.storage.codec;

import org.jetbrains.annotations.NotNull;

public interface ModelCodec {
	@FunctionalInterface
	interface Writer<T, ReadType> {
		@NotNull ReadType serialize(@NotNull T object);
	}

	@FunctionalInterface
	interface Reader<T, ReadType, Reader extends ModelReader<?, ReadType>> {
		@NotNull T deserialize(@NotNull Reader reader);
	}
}
