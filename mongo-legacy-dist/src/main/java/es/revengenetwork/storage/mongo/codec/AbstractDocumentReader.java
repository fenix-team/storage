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

public class AbstractDocumentReader<This extends ModelReader<This, Document>>
  implements ModelReader<This, Document> {

  protected final Document document;
  protected final Function<Document, This> readerFactory;

  protected AbstractDocumentReader(
    @NotNull Document document,
    @NotNull Function<Document, This> readerFactory
  ) {
    this.document = document;
    this.readerFactory = readerFactory;
  }

  @Override
  public @NotNull Document getRaw() {
    return document;
  }

  @Override
  public @Nullable Document readThis(@NotNull String field) {
    return document.get(field, Document.class);
  }

  @Override
  public String readString(@NotNull String field) {
    return document.getString(field);
  }

  @Override
  public Number readNumber(@NotNull String field) {
    return (Number) document.get(field);
  }

  @Override
  public Boolean readBoolean(@NotNull String field) {
    return document.getBoolean(field);
  }

  @Override
  public <T, C extends Collection<T>> @Nullable C readRawCollection(
    @NotNull String field, @NotNull Class<T> clazz,
    @NotNull Function<Integer, C> collectionFactory
  ) {
    List<?> value = document.get(field, List.class);

    if (value == null) {
      return null;
    }

    C collection = collectionFactory.apply(value.size());

    for (Object object : value) {
      collection.add(clazz.cast(object));
    }

    return collection;
  }

  @Override
  public <T> @Nullable T readObject(
    @NotNull String field,
    @NotNull ModelCodec.Reader<T, Document, This> reader
  ) {
    Document child = document.get(field, Document.class);

    if (child == null) {
      return null;
    }

    return reader.deserialize(readerFactory.apply(child));
  }

  @Override
  public <K, V> Map<K, V> readMap(
    @NotNull String field, @NotNull Function<V, K> keyParser,
    @NotNull ModelCodec.Reader<V, Document, This> reader
  ) {
    List<Document> documents = readRawCollection(field, Document.class, ArrayList::new);

    if (documents == null) {
      return null;
    }

    Map<K, V> map = new HashMap<>(documents.size());

    for (Document document : documents) {
      V value = reader.deserialize(readerFactory.apply(document));
      map.put(keyParser.apply(value), value);
    }

    return map;
  }

  @Override
  public <T, C extends Collection<T>> @Nullable C readCollection(
    @NotNull String field,
    @NotNull ModelCodec.Reader<T, Document, This> reader,
    @NotNull Function<Integer, C> collectionFactory
  ) {
    List<Document> documents = readRawCollection(field, Document.class, ArrayList::new);

    if (documents == null) {
      return null;
    }

    C children = collectionFactory.apply(documents.size());

    for (Document document : documents) {
      children.add(reader.deserialize(readerFactory.apply(document)));
    }

    return children;
  }
}
