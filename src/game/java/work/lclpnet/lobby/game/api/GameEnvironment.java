package work.lclpnet.lobby.game.api;

import net.minecraft.server.MinecraftServer;
import work.lclpnet.kibu.plugin.cmd.CommandStack;
import work.lclpnet.kibu.plugin.hook.HookStack;
import work.lclpnet.kibu.plugin.scheduler.SchedulerStack;
import work.lclpnet.mplugins.ext.Unloadable;

public interface GameEnvironment {

    MinecraftServer getServer();

    HookStack getHookStack();

    CommandStack getCommandStack();

    SchedulerStack getSchedulerStack();

    GameFinisher getFinisher();

    void closeWhenDone(Unloadable unloadable);
}
