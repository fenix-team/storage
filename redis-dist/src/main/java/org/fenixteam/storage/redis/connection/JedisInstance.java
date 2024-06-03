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
