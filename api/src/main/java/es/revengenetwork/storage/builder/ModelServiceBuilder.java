package es.revengenetwork.storage.builder;

import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public interface ModelServiceBuilder<T extends Model> {

  @Contract("_ -> this")
  ModelServiceBuilder<T> executor(@NotNull Executor executor);

  @Contract("_ -> this")
  ModelServiceBuilder<T> cachedService(@NotNull ModelService<T> cachedService);

  @Contract(" -> new")
  ModelService<T> build();
}
