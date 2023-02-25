package es.revengenetwork.storage.mongo.codec;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class DocumentReader
  extends AbstractDocumentReader<DocumentReader> {

  protected DocumentReader(final @NotNull Document document) {
    super(document, DocumentReader::create);
  }

  public static DocumentReader create(final @NotNull Document document) {
    return new DocumentReader(document);
  }
}
