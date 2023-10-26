package work.lclpnet.lobby.game;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public interface GameStateIo {

    CompletableFuture<Void> store();

    CompletableFuture<Void> restore(GameMangerLoader loader);

    @Nonnull
    GameManagerState getState();
}
