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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import org.fenixteam.storage.codec.ModelDeserializer;
import org.fenixteam.storage.codec.ModelSerializer;
import org.fenixteam.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@SuppressWarnings("unused")
public class RedisModelRepository<ModelType extends Model> extends AbstractAsyncModelRepository<ModelType> {
  protected final Gson gson;
  protected final ModelSerializer<ModelType, JsonObject> modelSerializer;
  protected final ModelDeserializer<ModelType, JsonObject> modelDeserializer;
  protected final JedisPool jedisPool;
  protected final String tableName;
  protected final int expireAfterSave;
  protected final int expireAfterAccess;

  protected RedisModelRepository(
    final @NotNull Executor executor,
    final @NotNull Gson gson,
    final @NotNull ModelSerializer<ModelType, JsonObject> modelSerializer,
    final @NotNull ModelDeserializer<ModelType, JsonObject> modelDeserializer,
    final @NotNull JedisPool jedisPool,
    final @NotNull String tableName,
    final int expireAfterSave,
    final int expireAfterAccess
  ) {
    super(executor);
    this.gson = gson;
    this.modelSerializer = modelSerializer;
    this.modelDeserializer = modelDeserializer;
    this.jedisPool = jedisPool;
    this.tableName = tableName;
    this.expireAfterSave = expireAfterSave;
    this.expireAfterAccess = expireAfterAccess;
  }

  @Contract(value = " -> new")
  public static <T extends Model> @NotNull RedisModelRepositoryBuilder<T> builder() {
    return new RedisModelRepositoryBuilder<>();
  }

  @Override
  public @NotNull ModelType saveSync(final @NotNull ModelType model) {
    try (final var jedis = this.jedisPool.getResource()) {
      final var object = this.modelSerializer.serialize(model);
      final var map = new HashMap<String, String>(object.size());
      for (final var entry : object.entrySet()) {
        map.put(entry.getKey(), this.gson.toJson(entry.getValue()));
      }
      final var key = this.tableName + ":" + model.id();
      jedis.hset(key, map);
      if (this.expireAfterSave > 0) {
        jedis.expire(key, this.expireAfterSave);
      }
      return model;
    }
  }

  @Override
  public boolean deleteSync(final @NotNull String id) {
    try (final var jedis = this.jedisPool.getResource()) {
      return jedis.del(this.tableName + ":" + id) > 0;
    }
  }

  @Override
  public @Nullable ModelType findSync(final @NotNull String id) {
    try (final var jedis = this.jedisPool.getResource()) {
      final var key = this.tableName + ":" + id;
      return this.readModel(jedis, key);
    }
  }

  @Override
  public <C extends Collection<String>> @Nullable C findIdsSync(final @NotNull Function<Integer, C> factory) {
    try (final var jedis = this.jedisPool.getResource()) {
      final var keys = jedis.keys(this.tableName + ":*");
      if (keys == null || keys.isEmpty()) {
        return null;
      }
      final var result = factory.apply(keys.size());
      for (final var key : keys) {
        result.add(key.substring(this.tableName.length() + 1));
      }
      return result;
    }
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    try (final var jedis = this.jedisPool.getResource()) {
      final var keys = jedis.keys(this.tableName + ":*");
      if (keys == null || keys.isEmpty()) {
        return null;
      }
      final var foundModels = factory.apply(keys.size());
      for (final var key : keys) {
        final var model = this.readModel(jedis, key);
        if (model == null) {
          continue;
        }
        postLoadAction.accept(model);
        foundModels.add(model);
      }
      return foundModels;
    }
  }

  @Override
  public boolean existsSync(final @NotNull String id) {
    try (final var jedis = this.jedisPool.getResource()) {
      return jedis.exists(this.tableName + ":" + id);
    }
  }

  protected @Nullable ModelType readModel(final @NotNull Jedis jedis, final @NotNull String key) {
    final var map = jedis.hgetAll(key);
    if (map.isEmpty()) {
      return null;
    }
    if (this.expireAfterAccess > 0) {
      jedis.expire(key, this.expireAfterAccess);
    }
    final var object = new JsonObject();
    for (final var entry : map.entrySet()) {
      object.add(entry.getKey(), this.gson.fromJson(entry.getValue(), JsonElement.class));
    }
    return this.modelDeserializer.deserialize(object);
  }
}
