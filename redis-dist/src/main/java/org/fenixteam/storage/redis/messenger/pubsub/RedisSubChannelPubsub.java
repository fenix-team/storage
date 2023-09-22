/*
 * This file is part of storage, licensed under the MIT License
 *
 * Copyright (c) 2023 FenixTeam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.fenixteam.storage.redis.messenger.pubsub;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.util.Map;
import org.fenixteam.storage.redis.channel.RedisChannel;
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
