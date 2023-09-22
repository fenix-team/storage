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
package org.fenixteam.storage.mongo.codec;

import java.util.Collection;
import java.util.UUID;
import org.bson.Document;
import org.fenixteam.storage.codec.AbstractObjectModelWriter;
import org.fenixteam.storage.codec.ModelWriter;
import org.fenixteam.storage.model.Model;
import org.fenixteam.storage.mongo.MongoModelRepository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class DocumentWriter extends AbstractObjectModelWriter<Document> {
  protected final Document document;

  protected DocumentWriter(final @NotNull Document document) {
    this.document = document;
  }

  @Contract(" -> new")
  public static @NotNull ModelWriter<Document> create() {
    return new DocumentWriter(new Document());
  }

  @Contract("_ -> new")
  public static @NotNull ModelWriter<Document> create(final @NotNull Document document) {
    return new DocumentWriter(document);
  }

  @Contract("_ -> new")
  public static @NotNull ModelWriter<Document> create(final @NotNull Model model) {
    return create().writeString(MongoModelRepository.ID_FIELD, model.id());
  }

  @Override
  @Contract("_, _ -> this")
  public @NotNull DocumentWriter writeObject(final @NotNull String field, final @Nullable Object value) {
    this.document.append(field, value);
    return this;
  }

  @Override
  public @NotNull ModelWriter<Document> writeDetailedUuid(final @NotNull String key, final @Nullable UUID uuid) {
    final var serializedUuid = this.writeDetailedUuid(uuid);
    if (serializedUuid == null) {
      return this;
    }
    this.document.append(key, serializedUuid);
    return this;
  }

  @Override
  public @NotNull ModelWriter<Document> writeDetailedUuids(
    final @NotNull String key,
    final @Nullable Collection<@NotNull UUID> uuids
  ) {
    if (uuids == null) {
      return this;
    }
    final var array = new Document();
    var index = 0;
    for (final var uuid : uuids) {
      final var serializedUuid = this.writeDetailedUuid(uuid);
      if (serializedUuid == null) {
        continue;
      }
      array.append(Integer.toString(index++), serializedUuid);
    }
    this.document.append(key, array);
    return this;
  }

  public @Nullable Document writeDetailedUuid(final @Nullable UUID uuid) {
    if (uuid == null) {
      return null;
    }
    final var serializedUuid = new Document();
    serializedUuid.append("least", uuid.getLeastSignificantBits());
    serializedUuid.append("most", uuid.getMostSignificantBits());
    return serializedUuid;
  }

  @Override
  public @NotNull Document current() {
    return this.document;
  }

  @Override
  public @NotNull Document end() {
    return this.document;
  }
}
