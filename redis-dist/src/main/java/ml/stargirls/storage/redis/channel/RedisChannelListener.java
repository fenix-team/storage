package ml.stargirls.storage.redis.channel;

import org.jetbrains.annotations.NotNull;

public interface RedisChannelListener<T> {

	void listen(@NotNull RedisChannel<T> channel, @NotNull String server, @NotNull T object);

	default void send(@NotNull RedisChannel<T> channel, @NotNull T object) { }
}
