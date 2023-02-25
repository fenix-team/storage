package es.revengenetwork.storage.repository.builder;

import es.revengenetwork.storage.repository.ModelRepository;
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
