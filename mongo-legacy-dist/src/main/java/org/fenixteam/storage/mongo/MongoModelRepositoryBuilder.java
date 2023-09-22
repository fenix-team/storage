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
