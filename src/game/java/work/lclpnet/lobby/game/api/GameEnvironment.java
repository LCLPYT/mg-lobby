package work.lclpnet.lobby.game.api;

import net.minecraft.server.MinecraftServer;
import work.lclpnet.kibu.plugin.cmd.CommandStack;
import work.lclpnet.kibu.plugin.hook.HookStack;
import work.lclpnet.kibu.plugin.scheduler.SchedulerStack;
import work.lclpnet.lobby.game.conf.GameConfig;
import work.lclpnet.lobby.game.map.MapManager;
import work.lclpnet.mplugins.ext.Unloadable;

import java.util.function.Supplier;

public interface GameEnvironment {

    MinecraftServer getServer();

    HookStack getHookStack();

    CommandStack getCommandStack();

    SchedulerStack getSchedulerStack();

    WorldFacade getWorldFacade(Supplier<MapManager> mapManagerSupplier);

    GameConfig getGameConfig();

    GameFinisher getFinisher();

    void closeWhenDone(Unloadable unloadable);
}
