package es.revengenetwork.storage.mongo.codec;

import es.revengenetwork.storage.codec.AbstractObjectModelWriter;
import es.revengenetwork.storage.codec.ModelWriter;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.mongo.MongoModelRepository;
import org.bson.Document;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * It's a builder for documents
 */
@SuppressWarnings("unused")
public class DocumentWriter extends AbstractObjectModelWriter<DocumentWriter, Document> {
  protected final Document document;

  protected DocumentWriter(final @NotNull Document document) {
    this.document = document;
  }

  @Contract(" -> new")
  public static @NotNull ModelWriter<DocumentWriter, Document> create() {
    return new DocumentWriter(new Document());
  }

  @Contract("_ -> new")
  public static @NotNull ModelWriter<DocumentWriter, Document> create(final @NotNull Document document) {
    return new DocumentWriter(document);
  }

  @Contract("_ -> new")
  public static @NotNull ModelWriter<DocumentWriter, Document> create(final @NotNull Model model) {
    return create().writeString(MongoModelRepository.ID_FIELD, model.getId());
  }

  @Override
  @Contract("_, _ -> this")
  public @NotNull DocumentWriter writeObject(final @NotNull String field, final @Nullable Object value) {
    this.document.append(field, value);
    return this;
  }

  @Override
  public @NotNull Document current() {
    return this.document;
  }

  @Override
  public @NotNull Document end() {
    return this.document;
  }
}
