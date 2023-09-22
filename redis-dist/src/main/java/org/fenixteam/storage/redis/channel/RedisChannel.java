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
package org.fenixteam.storage.redis.channel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.JedisPool;

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

  public @NotNull String name() {
    return this.name;
  }

  public @NotNull Type type() {
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

  public @NotNull Set<RedisChannelListener<MessageType>> listeners() {
    return this.listeners;
  }
}
