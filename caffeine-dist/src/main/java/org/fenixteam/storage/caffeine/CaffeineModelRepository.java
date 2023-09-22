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
package org.fenixteam.storage.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import org.fenixteam.storage.model.Model;
import org.fenixteam.storage.repository.ModelRepository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class CaffeineModelRepository<ModelType extends Model> implements ModelRepository<ModelType> {
  private final Cache<String, ModelType> cache;

  protected CaffeineModelRepository(final @NotNull Cache<String, ModelType> cache) {
    this.cache = cache;
  }

  @Contract(value = "_ -> new")
  public static <T extends Model> @NotNull CaffeineModelRepository<T> create(final @NotNull Cache<String, T> cache) {
    return new CaffeineModelRepository<>(cache);
  }

  @Override
  public @Nullable ModelType findSync(final @NotNull String id) {
    return this.cache.getIfPresent(id);
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findSync(
    final @NotNull String field,
    final @NotNull String value,
    final @NotNull Function<Integer, C> factory
  ) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public @Nullable Collection<String> findIdsSync() {
    return this.cache.asMap()
             .keySet();
  }

  @Override
  public <C extends Collection<ModelType>> @Nullable C findAllSync(
    final @NotNull Consumer<ModelType> postLoadAction,
    final @NotNull Function<Integer, C> factory
  ) {
    final var values = this.cache.asMap()
                         .values();
    final var foundModels = factory.apply(values.size());
    for (final var value : values) {
      postLoadAction.accept(value);
      foundModels.add(value);
    }
    return foundModels;
  }

  @Override
  public boolean existsSync(final @NotNull String id) {
    return this.cache.asMap()
             .containsKey(id);
  }

  @Override
  public @NotNull ModelType saveSync(final @NotNull ModelType model) {
    this.cache.put(model.id(), model);
    return model;
  }

  @Override
  public boolean deleteSync(final @NotNull String id) {
    this.cache.invalidate(id);
    return true;
  }
}
