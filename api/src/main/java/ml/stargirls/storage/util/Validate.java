package ml.stargirls.storage.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Validate {

	private Validate() {
		throw new UnsupportedOperationException();
	}

	public static <T> @NotNull T notNull(
			@Nullable T object, @NotNull String message,
			Object... replacements
	) {
		if (object == null) {
			throw new IllegalArgumentException(String.format(message, replacements));
		}

		return object;
	}

	public static <T> @NotNull T notNull(@Nullable T object) {
		return notNull(object, "Object cannot be null");
	}

	public static void state(
			boolean condition, @NotNull String message,
			Object... replacements
	) {
		if (!condition) {
			throw new IllegalStateException(String.format(message, replacements));
		}
	}

	public static void state(boolean condition) {
		state(condition, "Illegal state");
	}
}