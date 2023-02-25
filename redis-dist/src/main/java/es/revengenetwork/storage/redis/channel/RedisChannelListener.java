package es.revengenetwork.storage.redis.channel;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface RedisChannelListener<T> {

  void listen(
    final @NotNull RedisChannel<T> channel,
    final @NotNull String server,
    final @NotNull T object
  );

  default void send(final @NotNull RedisChannel<T> channel, final @NotNull T object) { }
}
