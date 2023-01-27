package ml.stargirls.storage.mongo.codec;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;

/**
 * It reads a document and converts it into a Java object
 */
public class DocumentReader
		extends AbstractDocumentReader<DocumentReader> {
	protected DocumentReader(@NotNull Document document) {
		super(document, DocumentReader::create);
	}

	public static DocumentReader create(@NotNull Document document) {
		return new DocumentReader(document);
	}
}
