package work.lclpnet.game.api.data;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface GameDataPacks {

    GameDataPacks EMPTY = (_, _) -> CompletableFuture.completedFuture(null);

    @NotNull CompletableFuture<Void> downloadPacks(@NotNull DataPackSink sink, @NotNull Executor executor);
}
