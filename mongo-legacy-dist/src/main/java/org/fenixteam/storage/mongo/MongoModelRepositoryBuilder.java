package org.fenixteam.storage.mongo;

import com.mongodb.client.MongoDatabase;
import java.util.concurrent.Executor;
import org.bson.Document;
import org.fenixteam.storage.codec.ModelDeserializer;
import org.fenixteam.storage.codec.ModelSerializer;
import org.fenixteam.storage.model.Model;
import org.fenixteam.storage.repository.AsyncModelRepository;
import org.fenixteam.storage.repository.builder.AbstractModelRepositoryBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class MongoModelRepositoryBuilder<ModelType extends Model>
  extends AbstractModelRepositoryBuilder<ModelType> {
  private MongoDatabase database;
  private String collectionName;
  private ModelSerializer<ModelType, Document> modelSerializer;
  private ModelDeserializer<ModelType, Document> modelDeserializer;

  MongoModelRepositoryBuilder() {
  }

  @Contract("_ -> this")
  public @NotNull MongoModelRepositoryBuilder<ModelType> database(final @NotNull MongoDatabase database) {
    this.database = database;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull MongoModelRepositoryBuilder<ModelType> modelSerializer(
    final @NotNull ModelSerializer<ModelType, Document> modelSerializer
  ) {
    this.modelSerializer = modelSerializer;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull MongoModelRepositoryBuilder<ModelType> modelDeserializer(
    final @NotNull ModelDeserializer<ModelType, Document> modelDeserializer
  ) {
    this.modelDeserializer = modelDeserializer;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull MongoModelRepositoryBuilder<ModelType> collection(final @NotNull String collection) {
    this.collectionName = collection;
    return this;
  }

  @Contract("_ -> new")
  public @NotNull AsyncModelRepository<ModelType> build(final @NotNull Executor executor) {
    final var collection = this.database.getCollection(this.collectionName);
    return new MongoModelRepository<>(executor, collection, this.modelSerializer, this.modelDeserializer);
  }
}
