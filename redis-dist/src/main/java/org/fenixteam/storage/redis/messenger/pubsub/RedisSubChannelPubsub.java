package org.fenixteam.storage.redis.messenger.pubsub;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import org.fenixteam.storage.redis.channel.RedisChannel;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPubSub;

public class RedisSubChannelPubsub extends JedisPubSub {
  private final String parentChannel;
  private final String serverId;
  private final Map<String, RedisChannel<?>> channels;

  public RedisSubChannelPubsub(
    final @NotNull String parentChannel,
    final @NotNull String serverId,
    final @NotNull Map<String, RedisChannel<?>> channels
  ) {
    this.parentChannel = parentChannel;
    this.serverId = serverId;
    this.channels = channels;
  }

  @Override
  public void onMessage(final @NotNull String channel, final @NotNull String message) {
    // we don't care if the message isn't from the parent channel
    if (!channel.equals(this.parentChannel)) {
      return;
    }
    // we can parse the message as a json object
    final JsonObject jsonObject;
    try (final var reader = new JsonReader(new StringReader(message))) {
      jsonObject = TypeAdapters.JSON_ELEMENT.read(reader).getAsJsonObject();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    final var serverId = jsonObject.get("server")
                           .getAsString();
    // if the message is from the server we're listening to
    if (serverId.equals(this.serverId)) {
      return;
    }
    final var targetServerElement = jsonObject.get("targetServer");
    if (targetServerElement != null) {
      final var targetServer = targetServerElement.getAsString();
      // if the message isn't for this server, ignore it
      if (!targetServer.equals(this.serverId)) {
        return;
      }
    }
    final var subChannel = jsonObject.get("channel")
                             .getAsString();
    @SuppressWarnings("unchecked") final var channelObject = (RedisChannel<Object>) this.channels.get(subChannel);
    // if the channel doesn't exist, we can't do anything
    if (channelObject == null) {
      return;
    }
    final var object = jsonObject.getAsJsonObject("message");
    final var deserializedObject = channelObject.deserializer()
                                     .deserialize(object);
    channelObject.listen(serverId, deserializedObject);
  }
}
