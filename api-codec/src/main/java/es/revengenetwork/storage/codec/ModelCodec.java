package es.revengenetwork.storage.codec;

import org.jetbrains.annotations.NotNull;

public interface ModelCodec {
  @FunctionalInterface
  interface Writer<ModelType, ReadType> {
    @NotNull ReadType serialize(final @NotNull ModelType object);
  }

  @FunctionalInterface
  interface Reader<ModelType, ReadType, Reader extends ModelReader<ReadType>> {
    @NotNull ModelType deserialize(final @NotNull Reader reader);
  }
}
