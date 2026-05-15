package work.lclpnet.game.api;

import org.jetbrains.annotations.NotNull;
import work.lclpnet.activity.Activity;
import work.lclpnet.game.map.MapManager;
import work.lclpnet.kibu.cmd.impl.CommandStack;
import work.lclpnet.kibu.hook.HookStack;
import work.lclpnet.kibu.scheduler.util.SchedulerStack;

import java.util.function.Supplier;

public interface GameEnvironment extends GameContext {

    @NotNull HookStack getHookStack();

    @NotNull CommandStack getCommandStack();

    @NotNull SchedulerStack getSchedulerStack();

    @NotNull WorldFacade getWorldFacade(@NotNull Supplier<MapManager> mapManagerSupplier);

    @NotNull GameFinisher getFinisher();

    void whenDone(@NotNull Runnable action);

    /**
     * Switches the current root activity.
     * Only one root activity may be active at a time.
     * Switching the activity will destroy the previous activity, e.g. the lobby activity or any other currently active activity.
     * @param activity The activity to make the new root activity.
     */
    void switchRootActivity(@NotNull Activity activity);
}
