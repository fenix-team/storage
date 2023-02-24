package es.revengenetwork.storage.builder;

import es.revengenetwork.storage.ModelRepository;
import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public interface ModelRepositoryBuilder<ModelType extends Model> {

  @Contract("_ -> this")
  ModelRepositoryBuilder<ModelType> executor(@NotNull Executor executor);

  @Contract("_ -> this")
  ModelRepositoryBuilder<ModelType> cachedService(@NotNull ModelRepository<ModelType> cachedService);

  @Contract(" -> new")
  ModelRepository<ModelType> build();
}
