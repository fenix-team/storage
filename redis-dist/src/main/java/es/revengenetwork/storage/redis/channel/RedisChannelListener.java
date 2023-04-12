package es.revengenetwork.storage.redis.channel;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface RedisChannelListener<MessageType> {
  void listen(
    final @NotNull RedisChannel<MessageType> channel,
    final @NotNull String server,
    final @NotNull MessageType object
  );

  default void send(final @NotNull RedisChannel<MessageType> channel, final @NotNull MessageType object) {

  }
}
