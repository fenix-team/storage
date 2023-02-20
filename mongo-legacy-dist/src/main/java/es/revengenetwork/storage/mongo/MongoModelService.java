package es.revengenetwork.storage.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import es.revengenetwork.storage.dist.RemoteModelService;
import es.revengenetwork.storage.model.Model;
import org.bson.Document;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public class MongoModelService<ModelType extends Model, Reader extends ModelReader<Reader, Document>>
  extends RemoteModelService<ModelType> {

  public static final String ID_FIELD = "_id";

  private final MongoCollection<Document> mongoCollection;
  private final Function<Document, Reader> readerFactory;
  private final ModelCodec.Writer<ModelType, Document> writer;
  private final ModelCodec.Reader<ModelType, Document, Reader> modelReader;

  protected MongoModelService(
    @NotNull Executor executor,
    @NotNull MongoCollection<Document> mongoCollection,
    @NotNull Function<Document, Reader> readerFactory,
    @NotNull ModelCodec.Writer<ModelType, Document> writer,
    @NotNull ModelCodec.Reader<ModelType, Document, Reader> modelReader
  ) {
    super(executor);

    this.mongoCollection = mongoCollection;
    this.readerFactory = readerFactory;
    this.writer = writer;
    this.modelReader = modelReader;
  }

  @Contract(pure = true, value = "_, _ -> new")
  public static <T extends Model, Reader extends ModelReader<Reader, Document>>
  @NotNull MongoModelServiceBuilder<T, Reader> builder(
    @NotNull Class<T> type,
    @NotNull Class<Reader> ignoredReaderType
  ) {
    return new MongoModelServiceBuilder<>(type);
  }

  @Override
  public @Nullable ModelType findSync(@NotNull String id) {
    Document document = mongoCollection
                          .find(Filters.eq(ID_FIELD, id))
                          .first();

    if (document == null) {
      return null;
    }

    return modelReader.deserialize(readerFactory.apply(document));
  }

  @Override
  public List<ModelType> findSync(@NotNull String field, @NotNull String value) {
    List<ModelType> models = new ArrayList<>();

    for (Document document : mongoCollection
                               .find(Filters.eq(field, value))) {
      models.add(modelReader.deserialize(readerFactory.apply(document)));
    }

    return models;
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    List<String> ids = new ArrayList<>();

    for (Document document : mongoCollection.find()) {
      ids.add(document.getString(ID_FIELD));
    }

    return ids;
  }

  @Override
  public List<ModelType> findAllSync(@NotNull Consumer<ModelType> postLoadAction) {
    List<Document> documents = mongoCollection.find()
                                 .into(new ArrayList<>());

    List<ModelType> models = new ArrayList<>();

    for (Document document : documents) {
      ModelType model = modelReader.deserialize(readerFactory.apply(document));
      postLoadAction.accept(model);
      models.add(model);
    }

    return models;
  }

  @Override
  public boolean existsSync(@NotNull final String id) {
    return mongoCollection.find(Filters.eq(ID_FIELD, id)).first() != null;
  }

  @Override
  public void saveSync(@NotNull ModelType model) {
    mongoCollection.replaceOne(
      Filters.eq(ID_FIELD, model.getId()),
      writer.serialize(model),
      new ReplaceOptions().upsert(true)
    );
  }

  @Override
  public boolean deleteSync(@NotNull String id) {
    return mongoCollection.deleteOne(Filters.eq(ID_FIELD, id))
             .wasAcknowledged();
  }
}
