package ml.stargirls.storage.gson.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ml.stargirls.storage.codec.ModelCodec;
import ml.stargirls.storage.codec.ModelWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public class JsonWriter
		implements ModelWriter<JsonWriter, JsonObject> {
	private final JsonObject jsonObject;

	protected JsonWriter(@NotNull JsonObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public static JsonWriter create() {
		return new JsonWriter(new JsonObject());
	}

	public static JsonWriter create(@NotNull JsonObject jsonObject) {
		return new JsonWriter(jsonObject);
	}

	public JsonWriter writeDetailedUuid(@NotNull String key, @Nullable UUID uuid) {
		if (uuid == null) {
			return this;
		}

		jsonObject.add(key, writeDetailedUuid(uuid));
		return this;
	}

	public JsonWriter writeDetailedUuids(@NotNull String key, @Nullable Collection<@NotNull UUID> uuids) {
		if (uuids == null) {
			return this;
		}

		JsonArray array = new JsonArray(uuids.size());

		for (UUID uuid : uuids) {
			JsonObject serializedUuid = writeDetailedUuid(uuid);

			if (serializedUuid == null) {
				continue;
			}

			array.add(serializedUuid);
		}

		jsonObject.add(key, array);
		return this;
	}

	public @Nullable JsonObject writeDetailedUuid(@Nullable UUID uuid) {
		if (uuid == null) {
			return null;
		}

		JsonObject serializedUuid = new JsonObject();
		serializedUuid.addProperty("least", uuid.getLeastSignificantBits());
		serializedUuid.addProperty("most", uuid.getMostSignificantBits());
		return serializedUuid;
	}

	@Override
	public JsonWriter writeThis(@NotNull String key, @Nullable JsonObject value) {
		jsonObject.add(key, value);
		return this;
	}

	@Override
	public JsonWriter writeUuid(@NotNull String field, @Nullable UUID uuid) {
		if (uuid == null) {
			return this;
		}

		jsonObject.addProperty(field, uuid.toString());
		return this;
	}

	@Override
	public JsonWriter writeString(@NotNull String field, @Nullable String value) {
		if (value == null) {
			return this;
		}

		jsonObject.addProperty(field, value);
		return this;
	}

	@Override
	public JsonWriter writeNumber(@NotNull String field, @Nullable Number value) {
		if (value == null) {
			return this;
		}

		jsonObject.addProperty(field, value);
		return this;
	}

	@Override
	public JsonWriter writeBoolean(@NotNull String field, @Nullable Boolean value) {
		if (value == null) {
			return this;
		}

		jsonObject.addProperty(field, value);
		return this;
	}

	@Override
	public <T> JsonWriter writeObject(
			@NotNull String field, @Nullable T child,
			ModelCodec.@NotNull Writer<T, JsonObject> writer
	) {
		if (child == null) {
			return this;
		}

		jsonObject.add(field, writer.serialize(child));
		return this;
	}

	@Override
	public <T> JsonWriter writeRawCollection(@NotNull final String field, @Nullable final Collection<T> children) {
		if (children == null) {
			return this;
		}

		JsonArray array = new JsonArray(children.size());

		for (T child : children) {
			if (child == null) {
				continue;
			}

			array.add(child.toString());
		}

		jsonObject.add(field, array);
		return this;
	}

	@Override
	public <T> JsonWriter writeCollection(
			@NotNull String field, @Nullable Collection<T> children,
			ModelCodec.@NotNull Writer<T, JsonObject> writer
	) {
		if (children == null) {
			return this;
		}

		JsonArray array = new JsonArray(children.size());

		for (T child : children) {
			array.add(writer.serialize(child));
		}

		jsonObject.add(field, array);
		return this;
	}

	@Override
	public JsonWriter writeObject(@NotNull String field, Object value) {
		return this;
	}

	@Override
	public @NotNull JsonObject current() {
		return jsonObject;
	}

	@Override
	public @NotNull JsonObject end() {
		return jsonObject;
	}
}
