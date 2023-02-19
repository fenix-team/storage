package es.revengenetwork.storage.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.builder.LayoutModelServiceBuilder;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import es.revengenetwork.storage.dist.DelegatedCachedModelService;
import es.revengenetwork.storage.model.Model;
import org.bson.Document;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class MongoModelServiceBuilder<ModelType extends Model, Reader extends ModelReader<Reader, Document>>
  extends LayoutModelServiceBuilder<ModelType, MongoModelServiceBuilder<ModelType, Reader>> {

  private MongoDatabase database;
  private String collectionName;
  private Function<Document, Reader> readerFactory;
  private ModelCodec.Writer<ModelType, Document> modelWriter;
  private ModelCodec.Reader<ModelType, Document, Reader> modelReader;

  protected MongoModelServiceBuilder(@NotNull Class<ModelType> type) {
    super(type);
  }

  @Contract("_ -> this")
  public MongoModelServiceBuilder<ModelType, Reader> database(@NotNull MongoDatabase database) {
    this.database = database;
    return this;
  }

  @Contract("_ -> this")
  public MongoModelServiceBuilder<ModelType, Reader> modelReader(
    @NotNull ModelCodec.Reader<ModelType, Document,
                                Reader> modelReader
  ) {
    this.modelReader = modelReader;
    return this;
  }

  @Contract("_ -> this")
  public MongoModelServiceBuilder<ModelType, Reader> modelWriter(@NotNull ModelCodec.Writer<ModelType, Document> modelWriter) {
    this.modelWriter = modelWriter;
    return this;
  }

  @Contract("_ -> this")
  public MongoModelServiceBuilder<ModelType, Reader> readerFactory(@NotNull Function<Document, Reader> readerFactory) {
    this.readerFactory = readerFactory;
    return this;
  }

  @Contract("_ -> this")
  public MongoModelServiceBuilder<ModelType, Reader> collection(@NotNull String collection) {
    this.collectionName = collection;
    return this;
  }

  @Override
  public ModelService<ModelType> build() {
    check();
    MongoCollection<Document> collection =
      database.getCollection(collectionName);

    MongoModelService<ModelType, Reader> modelService =
      new MongoModelService<>(executor, collection, readerFactory, modelWriter, modelReader);

    if (cacheModelService == null) {
      return modelService;
    } else {
      return new DelegatedCachedModelService<>(executor, cacheModelService, modelService);
    }
  }

  @Override
  protected MongoModelServiceBuilder<ModelType, Reader> back() {
    return this;
  }
}
