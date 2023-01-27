package ml.stargirls.storage.redis.connection;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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
		public @NotNull Builder setHost(@NotNull String host) {
			this.host = host;
			return this;
		}

		@Contract("_ -> this")
		public @NotNull Builder setPort(int port) {
			this.port = port;
			return this;
		}

		@Contract("_ -> this")
		public @NotNull Builder setPassword(@NotNull String password) {
			this.password = password;
			return this;
		}

		@Contract("_ -> this")
		public @NotNull Builder setTimeout(int timeout) {
			this.timeout = timeout;
			return this;
		}

		@Contract("_ -> this")
		public @NotNull Builder setConfig(@NotNull JedisPoolConfig config) {
			this.config = config;
			return this;
		}

		@Contract(" -> new")
		public @NotNull JedisInstance build() {
			Jedis jedis = new Jedis(host, port, timeout);

			JedisPool jedisPool;
			if (password == null || password.trim().isEmpty()) {
				jedisPool = new JedisPool(config, host, port, timeout);
			} else {
				jedisPool = new JedisPool(config, host, port, timeout, password);
				jedis.auth(password);
			}

			return new JedisInstance(jedis, jedisPool);
		}
	}
}
