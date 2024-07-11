package work.lclpnet.lobby.game;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface GameStateIo {

    CompletableFuture<Void> store();

    CompletableFuture<Void> restore(GameMangerLoader loader);

    @NotNull
    GameManagerState getState();
}
