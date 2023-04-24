package es.revengenetwork.storage.mongo.codec;

import es.revengenetwork.storage.codec.AbstractObjectModelWriter;
import es.revengenetwork.storage.codec.ModelWriter;
import es.revengenetwork.storage.model.Model;
import es.revengenetwork.storage.mongo.MongoModelRepository;
import java.util.Collection;
import java.util.UUID;
import org.bson.Document;
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
