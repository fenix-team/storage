package org.fenixteam.storage.redis.channel;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import org.fenixteam.storage.codec.ModelDeserializer;
import org.fenixteam.storage.codec.ModelSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.JedisPool;

@SuppressWarnings("unused")
public class RedisChannel<MessageType> {
  private final ModelDeserializer<MessageType, JsonObject> deserializer;
  private final JedisPool jedisPool;
  private final Set<RedisChannelListener<MessageType>> listeners;
  private final String name;
  private final String parentChannel;
  private final String serverId;
  private final ModelSerializer<MessageType, JsonObject> serializer;

  public RedisChannel(
    final @NotNull ModelDeserializer<MessageType, JsonObject> deserializer,
    final @NotNull String parentChannel,
    final @NotNull String serverId,
    final @NotNull String name,
    final @NotNull JedisPool jedisPool,
    final @NotNull ModelSerializer<MessageType, JsonObject> serializer
  ) {
    this.deserializer = deserializer;
    this.parentChannel = parentChannel;
    this.serverId = serverId;
    this.name = name;
    this.jedisPool = jedisPool;
    this.serializer = serializer;
    this.listeners = new HashSet<>();
  }

  public @NotNull ModelDeserializer<MessageType, JsonObject> deserializer() {
    return this.deserializer;
  }

  public @NotNull String name() {
    return this.name;
  }

  public void sendMessage(final @NotNull MessageType message, final @Nullable String targetServer) {
    final var objectToSend = new JsonObject();
    objectToSend.addProperty("channel", this.name);
    objectToSend.addProperty("server", this.serverId);
    if (targetServer != null) {
      objectToSend.addProperty("targetServer", targetServer);
    }
    objectToSend.add("message", this.serializer.serialize(message));
    final var stringWriter = new StringWriter();
    try (final var writer = new JsonWriter(stringWriter)) {
      writer.setSerializeNulls(false);
      TypeAdapters.JSON_ELEMENT.write(writer, objectToSend);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    try (final var jedis = this.jedisPool.getResource()) {
      jedis.publish(this.parentChannel, stringWriter.toString());
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

  public @NotNull Set<RedisChannelListener<MessageType>> listeners() {
    return this.listeners;
  }
}
