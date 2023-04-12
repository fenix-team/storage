package es.revengenetwork.storage.mongo.codec;

import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class DocumentReader implements ModelReader<Document> {
  protected final Document document;

  protected DocumentReader(final @NotNull Document document) {
    this.document = document;
  }

  @Override
  public @NotNull Document getRaw() {
    return this.document;
  }

  @Override
  public @Nullable Document readThis(final @NotNull String field) {
    return this.document.get(field, Document.class);
  }

  @Override
  public @Nullable String readString(final @NotNull String field) {
    return this.document.getString(field);
  }

  @Override
  public @Nullable Number readNumber(final @NotNull String field) {
    return (Number) this.document.get(field);
  }

  @Override
  public boolean readBoolean(final @NotNull String field) {
    final var value = this.document.getBoolean(field);
    if (value == null) {
      return false;
    }
    return value;
  }

  @Override
  public <T, C extends Collection<T>> @Nullable C readRawCollection(
    final @NotNull String field,
    final @NotNull Class<T> clazz,
    final @NotNull Function<Integer, C> collectionFactory
  ) {
    final var value = this.document.get(field, List.class);
    if (value == null) {
      return null;
    }
    final var collection = collectionFactory.apply(value.size());
    for (final var object : value) {
      collection.add(clazz.cast(object));
    }
    return collection;
  }

  @Override
  public <T, R extends ModelReader<Document>> @Nullable T readObject(
    final @NotNull String field,
    final @NotNull Function<Document, R> readerFactory,
    final ModelCodec.@NotNull Reader<T, Document, R> reader
  ) {
    final var child = this.document.get(field, Document.class);
    if (child == null) {
      return null;
    }
    return reader.deserialize(readerFactory.apply(child));
  }

  @Override
  public @Nullable <K, V, R extends ModelReader<Document>> Map<K, V> readMap(
    final @NotNull String field,
    final @NotNull Function<V, K> keyParser,
    final @NotNull Function<Document, R> readerFactory,
    final ModelCodec.@NotNull Reader<V, Document, R> reader
  ) {
    final var documents = this.readRawCollection(field, Document.class, ArrayList::new);
    if (documents == null) {
      return null;
    }
    final var map = new HashMap<K, V>(documents.size());
    for (final var document : documents) {
      final var value = reader.deserialize(readerFactory.apply(document));
      map.put(keyParser.apply(value), value);
    }
    return map;
  }

  @Override
  public <T, C extends Collection<T>, R extends ModelReader<Document>> @Nullable C readCollection(
    final @NotNull String field,
    final ModelCodec.@NotNull Reader<T, Document, R> reader,
    final @NotNull Function<Document, R> readerFactory,
    final @NotNull Function<Integer, C> collectionFactory
  ) {
    final var documents = this.readRawCollection(field, Document.class, ArrayList::new);
    if (documents == null) {
      return null;
    }
    final var children = collectionFactory.apply(documents.size());
    for (final var document : documents) {
      children.add(reader.deserialize(readerFactory.apply(document)));
    }
    return children;
  }
}
