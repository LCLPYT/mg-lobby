package work.lclpnet.lobby.game.api.data;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface GameDataPacks {

    GameDataPacks EMPTY = (sink, executor) -> CompletableFuture.completedFuture(null);

    CompletableFuture<Void> downloadPacks(DataPackSink sink, Executor executor);
}
