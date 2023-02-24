package es.revengenetwork.storage.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import es.revengenetwork.storage.ModelRepository;
import es.revengenetwork.storage.builder.LayoutModelRepositoryBuilder;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import es.revengenetwork.storage.dist.DelegatedCachedModelRepository;
import es.revengenetwork.storage.model.Model;
import org.bson.Document;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class MongoModelRepositoryBuilder<ModelType extends Model, Reader extends ModelReader<Reader, Document>>
  extends LayoutModelRepositoryBuilder<ModelType, MongoModelRepositoryBuilder<ModelType, Reader>> {

  private MongoDatabase database;
  private String collectionName;
  private Function<Document, Reader> readerFactory;
  private ModelCodec.Writer<ModelType, Document> modelWriter;
  private ModelCodec.Reader<ModelType, Document, Reader> modelReader;

  protected MongoModelRepositoryBuilder(@NotNull Class<ModelType> type) {
    super(type);
  }

  @Contract("_ -> this")
  public MongoModelRepositoryBuilder<ModelType, Reader> database(@NotNull MongoDatabase database) {
    this.database = database;
    return this;
  }

  @Contract("_ -> this")
  public MongoModelRepositoryBuilder<ModelType, Reader> modelReader(
    @NotNull ModelCodec.Reader<ModelType, Document,
                                Reader> modelReader
  ) {
    this.modelReader = modelReader;
    return this;
  }

  @Contract("_ -> this")
  public MongoModelRepositoryBuilder<ModelType, Reader> modelWriter(@NotNull ModelCodec.Writer<ModelType, Document> modelWriter) {
    this.modelWriter = modelWriter;
    return this;
  }

  @Contract("_ -> this")
  public MongoModelRepositoryBuilder<ModelType, Reader> readerFactory(@NotNull Function<Document, Reader> readerFactory) {
    this.readerFactory = readerFactory;
    return this;
  }

  @Contract("_ -> this")
  public MongoModelRepositoryBuilder<ModelType, Reader> collection(@NotNull String collection) {
    this.collectionName = collection;
    return this;
  }

  @Override
  public ModelRepository<ModelType> build() {
    check();
    MongoCollection<Document> collection =
      database.getCollection(collectionName);

    MongoModelRepository<ModelType, Reader> modelService =
      new MongoModelRepository<>(executor, collection, readerFactory, modelWriter, modelReader);

    if (cacheModelRepository == null) {
      return modelService;
    } else {
      return new DelegatedCachedModelRepository<>(executor, cacheModelRepository, modelService);
    }
  }

  @Override
  protected MongoModelRepositoryBuilder<ModelType, Reader> back() {
    return this;
  }
}
