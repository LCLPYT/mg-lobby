package work.lclpnet.game.api;

import work.lclpnet.activity.Activity;
import work.lclpnet.game.map.MapManager;
import work.lclpnet.kibu.cmd.impl.CommandStack;
import work.lclpnet.kibu.hook.HookStack;
import work.lclpnet.kibu.scheduler.util.SchedulerStack;

import java.util.function.Supplier;

public interface GameEnvironment extends GameContext {

    HookStack getHookStack();

    CommandStack getCommandStack();

    SchedulerStack getSchedulerStack();

    WorldFacade getWorldFacade(Supplier<MapManager> mapManagerSupplier);

    GameFinisher getFinisher();

    void whenDone(Runnable action);

    /**
     * Switches the current root activity.
     * Only one root activity may be active at a time.
     * Switching the activity will destroy the previous activity, e.g. the lobby activity or any other currently active activity.
     * @param activity The activity to make the new root activity.
     */
    void switchRootActivity(Activity activity);
}
