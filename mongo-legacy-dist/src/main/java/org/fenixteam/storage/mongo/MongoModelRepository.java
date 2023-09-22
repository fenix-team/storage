/*
 * This file is part of storage, licensed under the MIT License
 *
 * Copyright (c) 2023 FenixTeam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.fenixteam.storage.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReplaceOptions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bson.Document;
import org.fenixteam.storage.codec.ModelDeserializer;
import org.fenixteam.storage.codec.ModelSerializer;
import org.fenixteam.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class MongoModelRepository<ModelType extends Model> extends AbstractAsyncModelRepository<ModelType> {
  public static final String ID_FIELD = "_id";
  protected final MongoCollection<Document> mongoCollection;
  protected final ModelSerializer<ModelType, Document> modelSerializer;
  protected final ModelDeserializer<ModelType, Document> modelDeserializer;

  protected MongoModelRepository(
    final @NotNull Executor executor,
    final @NotNull MongoCollection<Document> mongoCollection,
    final @NotNull ModelSerializer<ModelType, Document> modelSerializer,
    final @NotNull ModelDeserializer<ModelType, Document> modelDeserializer
  ) {
    super(executor);
    this.mongoCollection = mongoCollection;
    this.modelSerializer = modelSerializer;
    this.modelDeserializer = modelDeserializer;
  }

  @Contract(value = " -> new")
  public static <T extends Model> @NotNull MongoModelRepositoryBuilder<T> builder() {
    return new MongoModelRepositoryBuilder<>();
  }

  @Override
  public @Nullable ModelType findSync(final @NotNull String id) {
    final var document = this.mongoCollection.find(Filters.eq(ID_FIELD, id))
                           .first();
    if (document == null) {
      return null;
    }
    return this.modelDeserializer.deserialize(document);
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findSync(
    final @NotNull String field,
    final @NotNull String value,
    final @NotNull Function<Integer, C> factory
  ) {
    final var foundModels = factory.apply(1);
    for (final var document : this.mongoCollection.find(Filters.eq(field, value))) {
      foundModels.add(this.modelDeserializer.deserialize(document));
    }
    return null;
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    final var ids = new ArrayList<String>();
    for (final var document : this.mongoCollection.find(Projections.include(ID_FIELD))) {
      ids.add(document.getString(ID_FIELD));
    }
    return ids;
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    final var documents = this.mongoCollection.find()
                            .into(new ArrayList<>());
    final var foundModels = factory.apply(documents.size());
    for (final var document : documents) {
      final var model = this.modelDeserializer.deserialize(document);
      postLoadAction.accept(model);
      foundModels.add(model);
    }
    return foundModels;
  }

  @Override
  public boolean existsSync(final @NotNull String id) {
    return this.mongoCollection.find(Filters.and(
        Filters.eq(ID_FIELD, id),
        Projections.include(ID_FIELD)))
             .first() != null;
  }

  @Override
  public @NotNull ModelType saveSync(final @NotNull ModelType model) {
    this.mongoCollection.replaceOne(
      Filters.eq(ID_FIELD, model.id()),
      this.modelSerializer.serialize(model),
      new ReplaceOptions().upsert(true)
    );
    return model;
  }

  @Override
  public boolean deleteSync(final @NotNull String id) {
    return this.mongoCollection.deleteOne(Filters.eq(ID_FIELD, id))
             .wasAcknowledged();
  }
}
