package org.fenixteam.storage.redis.messenger;

import com.google.gson.JsonObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import org.fenixteam.storage.codec.ModelDeserializer;
import org.fenixteam.storage.codec.ModelSerializer;
import org.fenixteam.storage.redis.channel.RedisChannel;
import org.fenixteam.storage.redis.connection.JedisInstance;
import org.fenixteam.storage.redis.messenger.pubsub.RedisSubChannelPubsub;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

@SuppressWarnings("unused")
public class RedisMessenger {
  private final String parentChannel;
  private final String serverId;
  private final JedisPool jedisPool;
  private final Map<String, RedisChannel<?>> channels;
  private final JedisPubSub pubSub;

  public RedisMessenger(
    final @NotNull String parentChannel,
    final @NotNull String serverId,
    final @NotNull Executor executor,
    final @NotNull JedisInstance jedisInstance
  ) {
    this.parentChannel = parentChannel;
    this.serverId = serverId;
    this.jedisPool = jedisInstance.jedisPool();
    this.channels = new ConcurrentHashMap<>();
    this.pubSub = new RedisSubChannelPubsub(parentChannel, serverId, this.channels);
    //noinspection resource
    executor.execute(() -> jedisInstance.listenerConnection()
                             .subscribe(this.pubSub, parentChannel));
  }

  @Contract(value = "_, _, _ -> new")
  public <T> @NotNull RedisChannel<T> channel(
    final @NotNull String name,
    final @NotNull ModelSerializer<T, JsonObject> modelSerializer,
    final @NotNull ModelDeserializer<T, JsonObject> modelDeserializer
  ) {
    @SuppressWarnings("unchecked") final var channel = (RedisChannel<T>) this.channels.get(name);
    if (channel != null) {
      return channel;
    }
    final var newChannel = new RedisChannel<T>(
      modelDeserializer,
      this.parentChannel,
      this.serverId,
      name,
      this.jedisPool,
      modelSerializer);
    this.channels.put(name, newChannel);
    return newChannel;
  }

  public void close() {
    this.channels.clear();
    if (this.pubSub.isSubscribed()) {
      this.pubSub.unsubscribe();
    }
  }
}
