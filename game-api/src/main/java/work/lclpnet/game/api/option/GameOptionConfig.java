package work.lclpnet.game.api.option;

import org.jetbrains.annotations.NotNull;
import work.lclpnet.game.api.GameContext;

public interface GameOptionConfig {

    @NotNull GameContext getContext();

    @NotNull
    <T> VotingConfig registerVoting(@NotNull String name, @NotNull OptionVoting<T> voting);

    void addTimedAction(@NotNull Runnable runnable, int ticksBeforeStart);
}
