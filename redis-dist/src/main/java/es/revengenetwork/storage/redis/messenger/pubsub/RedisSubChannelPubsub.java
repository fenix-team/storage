package es.revengenetwork.storage.redis.messenger.pubsub;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import es.revengenetwork.storage.redis.channel.RedisChannel;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPubSub;

public class RedisSubChannelPubsub extends JedisPubSub {
  private final String parentChannel;
  private final String serverId;
  private final Gson gson;
  private final Map<String, RedisChannel<?>> channels;

  public RedisSubChannelPubsub(
    final @NotNull String parentChannel,
    final @NotNull String serverId,
    final @NotNull Gson gson,
    final @NotNull Map<String, RedisChannel<?>> channels
  ) {
    this.parentChannel = parentChannel;
    this.serverId = serverId;
    this.gson = gson;
    this.channels = channels;
  }

  @Override
  public void onMessage(final @NotNull String channel, final @NotNull String message) {
    // we don't care if the message isn't from the parent channel
    if (!channel.equals(this.parentChannel)) {
      return;
    }
    // we can parse the message as a json object
    final var jsonMessage = JsonParser.parseString(message)
                              .getAsJsonObject();
    final var serverId = jsonMessage.get("server")
                           .getAsString();
    // if the message is from the server we're listening to
    if (serverId.equals(this.serverId)) {
      return;
    }
    final var targetServerElement = jsonMessage.get("targetServer");
    if (targetServerElement != null) {
      final var targetServer = targetServerElement.getAsString();
      // if the message isn't for this server, ignore it
      if (!targetServer.equals(this.serverId)) {
        return;
      }
    }
    final var subChannel = jsonMessage.get("channel")
                             .getAsString();
    @SuppressWarnings("unchecked") final var channelObject = (RedisChannel<Object>) this.channels.get(subChannel);
    // if the channel doesn't exist, we can't do anything
    if (channelObject == null) {
      return;
    }
    final var object = jsonMessage.get("message");
    final var deserializedObject = this.gson.fromJson(object, channelObject.type());
    channelObject.listen(serverId, deserializedObject);
  }
}
