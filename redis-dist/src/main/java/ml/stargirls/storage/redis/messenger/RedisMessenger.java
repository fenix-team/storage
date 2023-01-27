package ml.stargirls.storage.redis.messenger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ml.stargirls.storage.redis.channel.RedisChannel;
import ml.stargirls.storage.redis.connection.JedisInstance;
import ml.stargirls.storage.redis.messenger.pubsub.RedisSubChannelPubsub;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class RedisMessenger {
	private final String parentChannel;
	private final String serverId;
	private final Gson gson;

	private final JedisPool jedisPool;

	private final Map<String, RedisChannel<?>> channels;

	private final JedisPubSub pubSub;

	public RedisMessenger(
			@NotNull String parentChannel, @NotNull String serverId,
			@NotNull Executor executor, Gson gson,
			@NotNull JedisInstance jedisInstance
	) {
		this.parentChannel = parentChannel;
		this.serverId = serverId;
		this.gson = gson;
		this.jedisPool = jedisInstance.jedisPool();

		this.channels = new ConcurrentHashMap<>();
		pubSub = new RedisSubChannelPubsub(parentChannel, serverId, gson, channels);

		executor.execute(() ->
				                 jedisInstance.listenerConnection().subscribe(
						                 pubSub, parentChannel
				                 ));
	}

	@Contract(pure = true, value = "_, _ -> new")
	public <T> @NotNull RedisChannel<T> getChannel(@NotNull String name, @NotNull Class<T> type) {
		return getChannel(name, TypeToken.get(type));
	}

	public <T> @NotNull RedisChannel<T> getChannel(@NotNull String name, @NotNull TypeToken<T> type) {
		@SuppressWarnings("unchecked")
		RedisChannel<T> channel = (RedisChannel<T>) channels.get(name);
		Type rawType = type.getType();

		if (channel == null) {
			channel = new RedisChannel<>(parentChannel, serverId, name, rawType, jedisPool, gson);
			channels.put(name, channel);
		} else {
			if (!channel.getType().equals(rawType)) {
				throw new IllegalArgumentException("Channel type mismatch");
			}
		}

		return channel;
	}

	public void close() {
		channels.clear();

		if (pubSub.isSubscribed()) {
			pubSub.unsubscribe();
		}
	}
}
