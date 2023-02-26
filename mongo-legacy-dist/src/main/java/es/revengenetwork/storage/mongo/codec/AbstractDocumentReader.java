package es.revengenetwork.storage.mongo.codec;

import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class AbstractDocumentReader<This extends ModelReader<This, Document>>
  implements ModelReader<This, Document> {

  protected final Document document;
  protected final Function<Document, This> readerFactory;

  protected AbstractDocumentReader(
    final @NotNull Document document,
    final @NotNull Function<Document, This> readerFactory
  ) {
    this.document = document;
    this.readerFactory = readerFactory;
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
    final Boolean value = this.document.getBoolean(field);

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
    final List<?> value = this.document.get(field, List.class);

    if (value == null) {
      return null;
    }

    final C collection = collectionFactory.apply(value.size());

    for (final Object object : value) {
      collection.add(clazz.cast(object));
    }

    return collection;
  }

  @Override
  public <T> @Nullable T readObject(
    final @NotNull String field,
    final @NotNull ModelCodec.Reader<T, Document, This> reader
  ) {
    final Document child = this.document.get(field, Document.class);

    if (child == null) {
      return null;
    }

    return reader.deserialize(readerFactory.apply(child));
  }

  @Override
  public <K, V> @Nullable Map<K, V> readMap(
    final @NotNull String field,
    final @NotNull Function<V, K> keyParser,
    final @NotNull ModelCodec.Reader<V, Document, This> reader
  ) {
    final List<Document> documents = this.readRawCollection(field, Document.class, ArrayList::new);

    if (documents == null) {
      return null;
    }

    final Map<K, V> map = new HashMap<>(documents.size());

    for (final Document document : documents) {
      final V value = reader.deserialize(readerFactory.apply(document));
      map.put(keyParser.apply(value), value);
    }

    return map;
  }

  @Override
  public <T, C extends Collection<T>> @Nullable C readCollection(
    final @NotNull String field,
    final @NotNull ModelCodec.Reader<T, Document, This> reader,
    final @NotNull Function<Integer, C> collectionFactory
  ) {
    final List<Document> documents = this.readRawCollection(field, Document.class, ArrayList::new);

    if (documents == null) {
      return null;
    }

    final C children = collectionFactory.apply(documents.size());

    for (final Document document : documents) {
      children.add(reader.deserialize(readerFactory.apply(document)));
    }

    return children;
  }
}
