package work.lclpnet.lobby.game.api;

import work.lclpnet.kibu.cmd.impl.CommandStack;
import work.lclpnet.kibu.hook.HookStack;
import work.lclpnet.kibu.scheduler.util.SchedulerStack;
import work.lclpnet.lobby.game.map.MapManager;

import java.util.function.Supplier;

public interface GameEnvironment extends GameContext {

    HookStack getHookStack();

    CommandStack getCommandStack();

    SchedulerStack getSchedulerStack();

    WorldFacade getWorldFacade(Supplier<MapManager> mapManagerSupplier);

    GameFinisher getFinisher();

    void whenDone(Runnable action);
}
