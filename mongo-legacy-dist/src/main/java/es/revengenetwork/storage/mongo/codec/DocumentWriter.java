package es.revengenetwork.storage.mongo.codec;

import es.revengenetwork.storage.codec.DelegateObjectModelWriter;
import es.revengenetwork.storage.codec.ModelWriter;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.mongo.MongoModelRepository;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * It's a builder for documents
 */
public class DocumentWriter
  extends DelegateObjectModelWriter<DocumentWriter, Document> {

  protected final Document document;

  protected DocumentWriter(@NotNull Document document) {
    this.document = document;
  }

  public static ModelWriter<DocumentWriter, Document> create() {
    return new DocumentWriter(new Document());
  }

  public static ModelWriter<DocumentWriter, Document> create(@NotNull Document document) {
    return new DocumentWriter(document);
  }

  public static ModelWriter<DocumentWriter, Document> create(@NotNull Model model) {
    return create().writeString(MongoModelRepository.ID_FIELD, model.getId());
  }

  /**
   * It adds a field to the document.
   *
   * @param field
   *   The name of the field to be added to the document.
   * @param value
   *   The value to be written.
   *
   * @return Nothing.
   */
  @Override
  public DocumentWriter writeObject(@NotNull String field, @Nullable Object value) {
    document.append(field, value);
    return this;
  }

  @Override
  public @NotNull Document current() {
    return document;
  }

  @Override
  public @NotNull Document end() {
    return document;
  }
}
