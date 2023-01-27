package ml.stargirls.storage.gson.resolve;

import com.google.gson.*;

import java.lang.reflect.Type;

public class PolymorphismTypeAdapter<T>
		implements JsonSerializer<T>, JsonDeserializer<T> {

	private static final String CLASS_KEY = "class";
	private static final String VALUE_KEY = "value";

	@Override
	public T deserialize(
			JsonElement json, Type typeOfT,
			JsonDeserializationContext context
	) throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();
		JsonPrimitive primitive = jsonObject.get(CLASS_KEY)
				                          .getAsJsonPrimitive();

		String className = primitive.getAsString();
		try {
			@SuppressWarnings("unchecked")
			Class<? extends T> clazz = (Class<? extends T>)
					                           Class.forName(className);

			return context.deserialize(jsonObject.get(VALUE_KEY), clazz);
		} catch (ClassNotFoundException e) {
			throw new JsonParseException(e);
		}
	}

	@Override
	public JsonElement serialize(
			T src, Type typeOfSrc,
			JsonSerializationContext context
	) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(CLASS_KEY, src.getClass().getName());
		jsonObject.add(VALUE_KEY, context.serialize(src));
		return jsonObject;
	}
}
