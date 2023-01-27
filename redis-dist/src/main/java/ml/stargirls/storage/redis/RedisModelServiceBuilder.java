package ml.stargirls.storage.redis;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ml.stargirls.storage.ModelService;
import ml.stargirls.storage.builder.LayoutModelServiceBuilder;
import ml.stargirls.storage.codec.ModelCodec;
import ml.stargirls.storage.codec.ModelReader;
import ml.stargirls.storage.dist.DelegatedCachedModelService;
import ml.stargirls.storage.model.Model;
import ml.stargirls.storage.util.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPool;

import java.util.function.Function;

public class RedisModelServiceBuilder<T extends Model, Reader extends ModelReader<Reader, JsonObject>>
		extends LayoutModelServiceBuilder<T, RedisModelServiceBuilder<T, Reader>> {

	private Gson gson;
	private String tableName;
	private int expireAfterSave;
	private int expireAfterAccess;
	private JedisPool jedisPool;
	private Function<JsonObject, Reader> readerFactory;
	private ModelCodec.Writer<T, JsonObject> writer;
	private ModelCodec.Reader<T, JsonObject, Reader> reader;

	protected RedisModelServiceBuilder(@NotNull Class<T> type) {
		super(type);
	}

	@Contract("_ -> this")
	public @NotNull RedisModelServiceBuilder<T, Reader> gson(@NotNull Gson gson) {
		this.gson = gson;
		return back();
	}

	@Contract("_ -> this")
	public @NotNull RedisModelServiceBuilder<T, Reader> tableName(@NotNull String tableName) {
		this.tableName = tableName;
		return back();
	}

	@Contract("_ -> this")
	public @NotNull RedisModelServiceBuilder<T, Reader> expireAfterSave(int expireAfterSave) {
		this.expireAfterSave = expireAfterSave;
		return back();
	}

	@Contract("_ -> this")
	public @NotNull RedisModelServiceBuilder<T, Reader> expireAfterAccess(int expireAfterAccess) {
		this.expireAfterAccess = expireAfterAccess;
		return back();
	}

	@Contract("_ -> this")
	public @NotNull RedisModelServiceBuilder<T, Reader> jedisPool(@NotNull JedisPool jedisPool) {
		this.jedisPool = jedisPool;
		return back();
	}

	@Contract("_ -> this")
	public RedisModelServiceBuilder<T, Reader> modelReader(@NotNull ModelCodec.Reader<T, JsonObject, Reader> reader) {
		this.reader = reader;
		return back();
	}

	@Contract("_ -> this")
	public RedisModelServiceBuilder<T, Reader> modelWriter(@NotNull ModelCodec.Writer<T, JsonObject> writer) {
		this.writer = writer;
		return back();
	}

	@Contract("_ -> this")
	public RedisModelServiceBuilder<T, Reader> readerFactory(@NotNull Function<JsonObject, Reader> readerFactory) {
		this.readerFactory = readerFactory;
		return back();
	}

	@Contract(" -> this")
	@Override
	protected @NotNull RedisModelServiceBuilder<T, Reader> back() {
		return this;
	}

	@Override
	public ModelService<T> build() {
		check();
		Validate.notNull(gson, "gson");
		Validate.notNull(tableName, "tableName");
		Validate.notNull(jedisPool, "jedisPool");

		if (expireAfterSave <= 0) {
			expireAfterSave = -1;
		}

		if (expireAfterAccess <= 0) {
			expireAfterAccess = -1;
		}

		ModelService<T> modelService = new RedisModelService<>(
				executor, gson, readerFactory, writer, reader, jedisPool,
				tableName, expireAfterSave, expireAfterAccess);

		if (cacheModelService == null) {
			return modelService;
		} else {
			return new DelegatedCachedModelService<>(executor, cacheModelService, modelService);
		}
	}
}
