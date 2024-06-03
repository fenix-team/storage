package org.fenixteam.storage.codec;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ModelDeserializer<ModelType, ReadType> {
  @NotNull ModelType deserialize(final @NotNull ReadType serialized);
}
