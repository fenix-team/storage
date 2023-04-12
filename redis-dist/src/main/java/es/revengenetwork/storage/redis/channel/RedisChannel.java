package es.revengenetwork.storage.redis.channel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class RedisChannel<MessageType> {
  private final String parentChannel;
  private final String serverId;
  private final String name;
  private final Type type;
  private final JedisPool jedisPool;
  private final Set<RedisChannelListener<MessageType>> listeners;
  private final Gson gson;

  public RedisChannel(
    final @NotNull String parentChannel,
    final @NotNull String serverId,
    final @NotNull String name,
    final @NotNull Type type,
    final @NotNull JedisPool jedisPool,
    final @NotNull Gson gson
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
    return this.name;
  }

  public @NotNull Type getType() {
    return this.type;
  }

  public void sendMessage(final @NotNull MessageType message, final @Nullable String targetServer) {
    final var objectToSend = new JsonObject();
    objectToSend.addProperty("channel", this.name);
    objectToSend.addProperty("server", this.serverId);
    if (targetServer != null) {
      objectToSend.addProperty("targetServer", targetServer);
    }
    final var serializedMessage = this.gson.toJsonTree(message, this.type);
    objectToSend.add("message", serializedMessage);
    final var json = objectToSend.toString();
    try (final var jedis = this.jedisPool.getResource()) {
      jedis.publish(this.parentChannel, json);
    }
  }

  public void sendMessage(final @NotNull MessageType message) {
    this.sendMessage(message, null);
  }

  public @NotNull RedisChannel<MessageType> addListener(final @NotNull RedisChannelListener<MessageType> listener) {
    this.listeners.add(listener);
    return this;
  }

  public void listen(final @NotNull String server, final @NotNull MessageType object) {
    for (final var listener : this.listeners) {
      listener.listen(this, server, object);
    }
  }

  public @NotNull Set<RedisChannelListener<MessageType>> getListeners() {
    return this.listeners;
  }
}
