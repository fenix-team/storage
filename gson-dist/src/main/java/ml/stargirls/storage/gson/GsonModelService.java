package ml.stargirls.storage.gson;

import com.google.gson.Gson;
import ml.stargirls.storage.ModelService;
import ml.stargirls.storage.dist.RemoteModelService;
import ml.stargirls.storage.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class GsonModelService<T extends Model>
		extends RemoteModelService<T> {

	public static <T extends Model> GsonModelServiceBuilder<T> builder(Class<T> type) {
		return new GsonModelServiceBuilder<>(type);
	}

	private final Gson gson;
	private final Class<T> type;
	private final File folder;

	protected GsonModelService(
			@NotNull Executor executor, @NotNull Gson gson,
			@NotNull Class<T> type, @NotNull File folder
	) {
		super(executor);
		this.gson = gson;
		this.type = type;
		this.folder = folder;
	}

	@Override
	public @Nullable T findSync(@NotNull String id) {
		return internalFind(getFile(id));
	}

	@Override
	public List<T> findSync(@NotNull String field, @NotNull String value) {
		if (!field.equals(ModelService.ID_FIELD)) {
			throw new IllegalArgumentException(
					"Only ID field is supported for sync find"
			);
		}

		return Collections.singletonList(findSync(value));
	}

	@Override
	public List<T> findAllSync(@NotNull Consumer<T> postLoadAction) {
		File[] listFiles = folder.listFiles();

		if (listFiles == null) {
			return Collections.emptyList();
		}

		List<T> models = new ArrayList<>();

		for (File file : listFiles) {
			T model = internalFind(file);

			if (model == null) {
				continue;
			}

			postLoadAction.accept(model);
			models.add(model);
		}

		return models;
	}

	@Override
	public void saveSync(@NotNull T model) {
		File file = getFile(model.getId());

		boolean write;

		if (!file.exists()) {
			try {
				write = file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			write = true;
		}

		if (write) {
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(gson.toJson(model, type));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public boolean deleteSync(@NotNull String id) {
		return getFile(id).delete();
	}

	private File getFile(String id) {
		return new File(folder, id + ".json");
	}

	private T internalFind(File file) {
		if (!file.exists()) {
			return null;
		}

		try (FileReader reader = new FileReader(file)) {
			return gson.fromJson(reader, type);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
