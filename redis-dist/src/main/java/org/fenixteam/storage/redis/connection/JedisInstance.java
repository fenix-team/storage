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
package org.fenixteam.storage.redis.connection;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@SuppressWarnings("unused")
public record JedisInstance(@NotNull Jedis listenerConnection, @NotNull JedisPool jedisPool) {
  @Contract(" -> new")
  public static @NotNull Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String host;
    private int port;
    private String password;
    private int timeout;
    private JedisPoolConfig config = new JedisPoolConfig();

    protected Builder() {
    }

    @Contract("_ -> this")
    public @NotNull Builder host(final @NotNull String host) {
      this.host = host;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder port(final int port) {
      this.port = port;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder password(final @NotNull String password) {
      this.password = password;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder timeout(final int timeout) {
      this.timeout = timeout;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder config(final @NotNull JedisPoolConfig config) {
      this.config = config;
      return this;
    }

    @Contract(" -> new")
    public @NotNull JedisInstance build() {
      final var jedis = new Jedis(this.host, this.port, this.timeout);
      final JedisPool jedisPool;
      if (this.password == null || this.password.trim()
                                     .isEmpty()) {
        jedisPool = new JedisPool(this.config, this.host, this.port, this.timeout);
      } else {
        jedisPool = new JedisPool(this.config, this.host, this.port, this.timeout, this.password);
        jedis.auth(this.password);
      }
      return new JedisInstance(jedis, jedisPool);
    }
  }
}
