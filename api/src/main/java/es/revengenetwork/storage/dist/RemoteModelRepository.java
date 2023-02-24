package es.revengenetwork.storage.dist;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public abstract class RemoteModelRepository<ModelType extends Model>
  extends AbstractAsyncModelRepository<ModelType> {

  public RemoteModelRepository(@NotNull Executor executor) {
    super(executor);
  }
}
