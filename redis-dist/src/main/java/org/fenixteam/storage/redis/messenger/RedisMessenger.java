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
package org.fenixteam.storage.redis.messenger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.revengenetwork.storage.redis.connection.JedisInstance;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import org.fenixteam.storage.redis.channel.RedisChannel;
import org.fenixteam.storage.redis.messenger.pubsub.RedisSubChannelPubsub;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

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
    this.pubSub = new RedisSubChannelPubsub(parentChannel, serverId, gson, this.channels);
    //noinspection resource
    executor.execute(() -> jedisInstance.listenerConnection()
                             .subscribe(this.pubSub, parentChannel));
  }

  @Contract(value = "_, _ -> new")
  public <T> @NotNull RedisChannel<T> channel(final @NotNull String name, final @NotNull Class<T> type) {
    return this.channel(name, TypeToken.get(type));
  }

  public <T> @NotNull RedisChannel<T> channel(final @NotNull String name, final @NotNull TypeToken<T> type) {
    @SuppressWarnings("unchecked") final var channel = (RedisChannel<T>) this.channels.get(name);
    final var rawType = type.getType();
    if (channel == null) {
      final var newChannel = new RedisChannel<T>(
        this.parentChannel,
        this.serverId,
        name,
        rawType,
        this.jedisPool,
        this.gson);
      this.channels.put(name, newChannel);
      return newChannel;
    } else {
      if (!channel.type()
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
