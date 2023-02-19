package es.revengenetwork.storage.dist;

import es.revengenetwork.storage.model.Model;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public abstract class RemoteModelService<T extends Model>
  extends AbstractAsyncModelService<T> {

  public RemoteModelService(@NotNull Executor executor) {
    super(executor);
  }
}
