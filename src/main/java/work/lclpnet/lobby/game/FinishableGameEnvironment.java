package work.lclpnet.lobby.game;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import work.lclpnet.kibu.plugin.cmd.CommandStack;
import work.lclpnet.kibu.plugin.hook.HookStack;
import work.lclpnet.kibu.plugin.scheduler.SchedulerStack;
import work.lclpnet.lobby.LobbyAPI;

public class FinishableGameEnvironment implements GameEnvironment, GameFinisher {

    private final MinecraftServer server;
    private final Logger logger;
    private volatile HookStack hookStack;
    private volatile CommandStack commandStack;
    private volatile SchedulerStack schedulerStack;

    public FinishableGameEnvironment(MinecraftServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public HookStack getHookStack() {
        if (hookStack != null) {
            return hookStack;
        }

        synchronized (this) {
            if (hookStack == null) {
                hookStack = new HookStack();
            }

            return hookStack;
        }
    }

    @Override
    public CommandStack getCommandStack() {
        if (commandStack != null) {
            return commandStack;
        }

        synchronized (this) {
            if (commandStack == null) {
                commandStack = new CommandStack();
            }

            return commandStack;
        }
    }

    @Override
    public SchedulerStack getSchedulerStack() {
        if (schedulerStack != null) {
            return schedulerStack;
        }

        synchronized (this) {
            if (schedulerStack == null) {
                schedulerStack = new SchedulerStack(logger);
            }

            return schedulerStack;
        }
    }

    @Override
    public GameFinisher getFinisher() {
        return this;
    }

    @Override
    public void finishGame() {
        server.submit(() -> {
            if (hookStack != null) {
                hookStack.unload();
            }

            if (commandStack != null) {
                commandStack.unload();
            }

            if (schedulerStack != null) {
                schedulerStack.unload();
            }

            LobbyAPI.getInstance().enterLobbyPhase();
        });
    }
}
