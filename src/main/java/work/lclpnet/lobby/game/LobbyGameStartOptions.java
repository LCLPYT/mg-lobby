package work.lclpnet.lobby.game;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import work.lclpnet.game.api.GameContext;
import work.lclpnet.game.api.option.GameStartOptions;

import java.util.*;

/**
 * Simple implementation of {@link GameStartOptions} and manager of the game start.
 * Lifetime bound to the selected game.
 * If the game-host changes the selected game, a new instance of this class is created.
 */
public class LobbyGameStartOptions implements GameStartOptions {

    private final GameContext context;
    private final Map<Integer, Set<Runnable>> timedActions = new HashMap<>();

    public LobbyGameStartOptions(GameContext context) {
        this.context = context;
    }

    @Override
    public @NonNull GameContext getContext() {
        return context;
    }

    @Override
    public synchronized void addTimedAction(int ticksBeforeStart, @NotNull Runnable runnable) {
        Objects.requireNonNull(runnable, "Action must not be null");

        if (ticksBeforeStart < 0) {
            throw new IllegalArgumentException("Ticks before start must not be negative");
        }

        Set<Runnable> actions = timedActions.computeIfAbsent(ticksBeforeStart, _ -> new HashSet<>());

        actions.add(runnable);
    }

    public void runTimedActions(int ticksRemaining) {
        Set<Runnable> actions;

        synchronized (this) {
            actions = timedActions.getOrDefault(ticksRemaining, Set.of());
        }

        actions.forEach(Runnable::run);
    }

}
