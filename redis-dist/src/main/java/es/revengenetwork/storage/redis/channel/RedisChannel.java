package es.revengenetwork.storage.redis.channel;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class RedisChannel<T> {

  private final String parentChannel;
  private final String serverId;
  private final String name;
  private final Type type;
  private final JedisPool jedisPool;
  private final Set<RedisChannelListener<T>> listeners;

  private final Gson gson;

  public RedisChannel(
    @NotNull String parentChannel, @NotNull String serverId,
    @NotNull String name, @NotNull Type type,
    @NotNull JedisPool jedisPool, @NotNull Gson gson
  ) {
    this.parentChannel = parentChannel;
    this.serverId = serverId;
    this.name = name;
    this.type = type;
    this.jedisPool = jedisPool;
    this.gson = gson;
    this.listeners = new HashSet<>();
  }

  public @NotNull String getName() {
    return name;
  }

  public @NotNull Type getType() {
    return type;
  }

  public void sendMessage(@NotNull T message, @Nullable String targetServer) {
    JsonObject objectToSend = new JsonObject();

    objectToSend.addProperty("channel", name);
    objectToSend.addProperty("server", serverId);

    if (targetServer != null) {
      objectToSend.addProperty("targetServer", targetServer);
    }

    JsonElement serializedMessage = gson.toJsonTree(message, type);
    objectToSend.add("message", serializedMessage);
    String json = objectToSend.toString();

    try (Jedis jedis = jedisPool.getResource()) {
      jedis.publish(parentChannel, json);
    }
  }

  public void sendMessage(@NotNull T message) {
    sendMessage(message, null);
  }

  public @NotNull RedisChannel<T> addListener(@NotNull RedisChannelListener<T> redisChannelListener) {
    listeners.add(redisChannelListener);
    return this;
  }

  public void listen(@NotNull String server, @NotNull T object) {
    for (RedisChannelListener<T> listener : listeners) {
      listener.listen(this, server, object);
    }
  }

  public @NotNull Set<RedisChannelListener<T>> getListeners() {
    return listeners;
  }
}
