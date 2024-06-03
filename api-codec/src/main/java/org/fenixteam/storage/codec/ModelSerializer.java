package org.fenixteam.storage.codec;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ModelSerializer<ModelType, ReadType> {
  @NotNull ReadType serialize(final @NotNull ModelType modelType);
}
