package es.revengenetwork.storage.mongo;

import com.mongodb.client.MongoDatabase;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.repository.AsyncModelRepository;
import es.revengenetwork.storage.repository.builder.AbstractModelRepositoryBuilder;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.bson.Document;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class MongoModelRepositoryBuilder<ModelType extends Model, Reader extends ModelReader<Document>>
  extends AbstractModelRepositoryBuilder<ModelType> {
  private MongoDatabase database;
  private String collectionName;
  private Function<Document, Reader> readerFactory;
  private ModelCodec.Writer<ModelType, Document> modelWriter;
  private ModelCodec.Reader<ModelType, Document, Reader> modelReader;

  protected MongoModelRepositoryBuilder() {
  }

  @Contract("_ -> this")
  public @NotNull MongoModelRepositoryBuilder<ModelType, Reader> database(final @NotNull MongoDatabase database) {
    this.database = database;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull MongoModelRepositoryBuilder<ModelType, Reader> modelReader(
    final ModelCodec.@NotNull Reader<ModelType, Document, Reader> modelReader
  ) {
    this.modelReader = modelReader;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull MongoModelRepositoryBuilder<ModelType, Reader> modelWriter(
    final ModelCodec.@NotNull Writer<ModelType, Document> modelWriter
  ) {
    this.modelWriter = modelWriter;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull MongoModelRepositoryBuilder<ModelType, Reader> readerFactory(
    final @NotNull Function<Document, Reader> readerFactory
  ) {
    this.readerFactory = readerFactory;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull MongoModelRepositoryBuilder<ModelType, Reader> collection(final @NotNull String collection) {
    this.collectionName = collection;
    return this;
  }

  @Contract("_ -> new")
  public @NotNull AsyncModelRepository<ModelType> build(final @NotNull Executor executor) {
    final var collection = this.database.getCollection(this.collectionName);
    return new MongoModelRepository<>(executor, collection, this.readerFactory, this.modelWriter, this.modelReader);
  }
}
