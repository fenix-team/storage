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
package org.fenixteam.storage.redis;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.concurrent.Executor;
import org.fenixteam.storage.codec.ModelDeserializer;
import org.fenixteam.storage.codec.ModelSerializer;
import org.fenixteam.storage.model.Model;
import org.fenixteam.storage.repository.AsyncModelRepository;
import org.fenixteam.storage.repository.builder.AbstractModelRepositoryBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPool;

@SuppressWarnings("unused")
public class RedisModelRepositoryBuilder<ModelType extends Model>
  extends AbstractModelRepositoryBuilder<ModelType> {
  private Gson gson;
  private String tableName;
  private int expireAfterSave;
  private int expireAfterAccess;
  private JedisPool jedisPool;
  private ModelSerializer<ModelType, JsonObject> modelSerializer;
  private ModelDeserializer<ModelType, JsonObject> modelDeserializer;

  protected RedisModelRepositoryBuilder() {
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType> gson(final @NotNull Gson gson) {
    this.gson = gson;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType> tableName(final @NotNull String tableName) {
    this.tableName = tableName;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType> expireAfterSave(final int expireAfterSave) {
    this.expireAfterSave = expireAfterSave;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType> expireAfterAccess(final int expireAfterAccess) {
    this.expireAfterAccess = expireAfterAccess;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull RedisModelRepositoryBuilder<ModelType> jedisPool(final @NotNull JedisPool jedisPool) {
    this.jedisPool = jedisPool;
    return this;
  }

  @Contract("_ -> this")
  public RedisModelRepositoryBuilder<ModelType> modelDeserializer(
    final @NotNull ModelDeserializer<ModelType, JsonObject> modelDeserializer
  ) {
    this.modelDeserializer = modelDeserializer;
    return this;
  }

  @Contract("_ -> this")
  public RedisModelRepositoryBuilder<ModelType> modelSerializer(
    final @NotNull ModelSerializer<ModelType, JsonObject> modelSerializer
  ) {
    this.modelSerializer = modelSerializer;
    return this;
  }

  @Contract("_ -> new")
  public @NotNull AsyncModelRepository<ModelType> build(final @NotNull Executor executor) {
    if (this.expireAfterSave <= 0) {
      this.expireAfterSave = -1;
    }
    if (this.expireAfterAccess <= 0) {
      this.expireAfterAccess = -1;
    }
    return new RedisModelRepository<>(
      executor,
      this.gson,
      this.modelSerializer,
      this.modelDeserializer,
      this.jedisPool,
      this.tableName,
      this.expireAfterSave,
      this.expireAfterAccess);
  }
}
