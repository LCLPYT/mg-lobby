package work.lclpnet.game.api.option;

import org.jetbrains.annotations.NotNull;
import work.lclpnet.game.api.GameContext;

public interface GameStartOptions {

    @NotNull GameContext getContext();

    void addTimedAction(int ticksBeforeStart, @NotNull Runnable runnable);
}
