package work.lclpnet.lobby.game.api;

import net.minecraft.server.MinecraftServer;
import work.lclpnet.kibu.cmd.impl.CommandStack;
import work.lclpnet.kibu.hook.HookStack;
import work.lclpnet.kibu.scheduler.util.SchedulerStack;
import work.lclpnet.lobby.game.conf.GameConfig;
import work.lclpnet.lobby.game.map.MapManager;

import java.util.function.Supplier;

public interface GameEnvironment {

    MinecraftServer getServer();

    HookStack getHookStack();

    CommandStack getCommandStack();

    SchedulerStack getSchedulerStack();

    WorldFacade getWorldFacade(Supplier<MapManager> mapManagerSupplier);

    GameConfig getGameConfig();

    GameFinisher getFinisher();

    void whenDone(Runnable action);
}
