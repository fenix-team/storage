package es.revengenetwork.storage.builder;

import es.revengenetwork.storage.ModelService;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public interface ModelServiceBuilder<ModelType extends Model> {

  @Contract("_ -> this")
  ModelServiceBuilder<ModelType> executor(@NotNull Executor executor);

  @Contract("_ -> this")
  ModelServiceBuilder<ModelType> cachedService(@NotNull ModelService<ModelType> cachedService);

  @Contract(" -> new")
  ModelService<ModelType> build();
}
