package es.revengenetwork.storage.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import es.revengenetwork.storage.codec.ModelCodec;
import es.revengenetwork.storage.codec.ModelReader;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.repository.AbstractAsyncModelRepository;
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

public class MongoModelRepository<ModelType extends Model, Reader extends ModelReader<Reader, Document>>
  extends AbstractAsyncModelRepository<ModelType> {

  public static final String ID_FIELD = "_id";

  private final MongoCollection<Document> mongoCollection;
  private final Function<Document, Reader> readerFactory;
  private final ModelCodec.Writer<ModelType, Document> writer;
  private final ModelCodec.Reader<ModelType, Document, Reader> modelReader;

  protected MongoModelRepository(
    final @NotNull Executor executor,
    final @NotNull MongoCollection<Document> mongoCollection,
    final @NotNull Function<Document, Reader> readerFactory,
    final @NotNull ModelCodec.Writer<ModelType, Document> writer,
    final @NotNull ModelCodec.Reader<ModelType, Document, Reader> modelReader
  ) {
    super(executor);

    this.mongoCollection = mongoCollection;
    this.readerFactory = readerFactory;
    this.writer = writer;
    this.modelReader = modelReader;
  }

  @Contract(pure = true, value = "_, _ -> new")
  public static <T extends Model, Reader extends ModelReader<Reader, Document>>
  @NotNull MongoModelRepositoryBuilder<T, Reader> builder(
    final @NotNull Class<T> type,
    final @NotNull Class<Reader> ignoredReaderType
  ) {
    return new MongoModelRepositoryBuilder<>(type);
  }

  @Override
  public @Nullable ModelType findSync(@NotNull String id) {
    Document document = this.mongoCollection.find(Filters.eq(ID_FIELD, id))
                          .first();

    if (document == null) {
      return null;
    }

    return this.modelReader.deserialize(this.readerFactory.apply(document));
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findSync(
    @NotNull final String field,
    @NotNull final String value,
    @NotNull final Function<Integer, C> factory
  ) {
    final C foundModels = factory.apply(1);

    for (final Document document : this.mongoCollection.find(Filters.eq(field, value))) {
      foundModels.add(this.modelReader.deserialize(this.readerFactory.apply(document)));
    }

    return null;
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
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    @NotNull final Consumer<ModelType> postLoadAction,
    @NotNull final Function<Integer, C> factory
  ) {
    final List<Document> documents = this.mongoCollection.find()
                                       .into(new ArrayList<>());
    final C foundModels = factory.apply(documents.size());

    for (final Document document : documents) {
      final ModelType model = this.modelReader.deserialize(this.readerFactory.apply(document));
      postLoadAction.accept(model);
      foundModels.add(model);
    }

    return foundModels;
  }

  @Override
  public boolean existsSync(@NotNull final String id) {
    return mongoCollection.find(Filters.eq(ID_FIELD, id))
             .first() != null;
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
