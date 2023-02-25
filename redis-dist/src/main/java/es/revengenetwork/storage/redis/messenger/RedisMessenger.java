package es.revengenetwork.storage.redis.messenger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.revengenetwork.storage.redis.channel.RedisChannel;
import es.revengenetwork.storage.redis.connection.JedisInstance;
import es.revengenetwork.storage.redis.messenger.pubsub.RedisSubChannelPubsub;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@SuppressWarnings("unused")
public class RedisMessenger {

  private final String parentChannel;
  private final String serverId;
  private final Gson gson;

  private final JedisPool jedisPool;

  private final Map<String, RedisChannel<?>> channels;

  private final JedisPubSub pubSub;

  public RedisMessenger(
    final @NotNull String parentChannel,
    final @NotNull String serverId,
    final @NotNull Executor executor,
    final @NotNull Gson gson,
    final @NotNull JedisInstance jedisInstance
  ) {
    this.parentChannel = parentChannel;
    this.serverId = serverId;
    this.gson = gson;
    this.jedisPool = jedisInstance.jedisPool();

    this.channels = new ConcurrentHashMap<>();
    this.pubSub = new RedisSubChannelPubsub(parentChannel, serverId, gson, channels);

    //noinspection resource
    executor.execute(() -> jedisInstance.listenerConnection()
                             .subscribe(pubSub, parentChannel));
  }

  @Contract(value = "_, _ -> new")
  public <T> @NotNull RedisChannel<T> getChannel(
    final @NotNull String name,
    final @NotNull Class<T> type
  ) {
    return this.getChannel(name, TypeToken.get(type));
  }

  public <T> @NotNull RedisChannel<T> getChannel(
    final @NotNull String name,
    final @NotNull TypeToken<T> type
  ) {
    @SuppressWarnings("unchecked")
    final RedisChannel<T> channel = (RedisChannel<T>) this.channels.get(name);
    final Type rawType = type.getType();

    if (channel == null) {
      final RedisChannel<T> newChannel = new RedisChannel<>(
        parentChannel,
        serverId,
        name,
        rawType,
        jedisPool,
        gson);
      this.channels.put(name, newChannel);
      return newChannel;
    } else {
      if (!channel.getType()
             .equals(rawType)) {
        throw new IllegalArgumentException("Channel type mismatch");
      }
    }

    return channel;
  }

  public void close() {
    this.channels.clear();

    if (this.pubSub.isSubscribed()) {
      this.pubSub.unsubscribe();
    }
  }
}
