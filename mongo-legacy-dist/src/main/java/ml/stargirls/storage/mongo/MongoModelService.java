package ml.stargirls.storage.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import ml.stargirls.storage.codec.ModelCodec;
import ml.stargirls.storage.codec.ModelReader;
import ml.stargirls.storage.dist.RemoteModelService;
import ml.stargirls.storage.model.Model;
import org.bson.Document;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public class MongoModelService<T extends Model, Reader extends ModelReader<Reader, Document>>
		extends RemoteModelService<T> {
	public static final String ID_FIELD = "_id";

	private final MongoCollection<Document> mongoCollection;
	private final Function<Document, Reader> readerFactory;
	private final ModelCodec.Writer<T, Document> writer;
	private final ModelCodec.Reader<T, Document, Reader> modelReader;

	protected MongoModelService(
			@NotNull Executor executor,
			@NotNull MongoCollection<Document> mongoCollection,
			@NotNull Function<Document, Reader> readerFactory,
			@NotNull ModelCodec.Writer<T, Document> writer,
			@NotNull ModelCodec.Reader<T, Document, Reader> modelReader
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
	public @Nullable T findSync(@NotNull String id) {
		Document document = mongoCollection
				                    .find(Filters.eq(ID_FIELD, id))
				                    .first();

		if (document == null) {
			return null;
		}

		return modelReader.deserialize(readerFactory.apply(document));
	}

	@Override
	public List<T> findSync(@NotNull String field, @NotNull String value) {
		List<T> models = new ArrayList<>();

		for (Document document : mongoCollection
				                         .find(Filters.eq(field, value))) {
			models.add(modelReader.deserialize(readerFactory.apply(document)));
		}

		return models;
	}

	@Override
	public List<T> findAllSync(@NotNull Consumer<T> postLoadAction) {
		List<Document> documents = mongoCollection.find()
				                           .into(new ArrayList<>());

		List<T> models = new ArrayList<>();

		for (Document document : documents) {
			T model = modelReader.deserialize(readerFactory.apply(document));
			postLoadAction.accept(model);
			models.add(model);
		}

		return models;
	}

	@Override
	public void saveSync(@NotNull T model) {
		mongoCollection.replaceOne(
				Filters.eq(ID_FIELD, model.getId()),
				writer.serialize(model),
				new ReplaceOptions().upsert(true)
		);
	}

	@Override
	public boolean deleteSync(@NotNull String id) {
		return mongoCollection.deleteOne(Filters.eq(ID_FIELD, id)).wasAcknowledged();
	}
}
