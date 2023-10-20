package work.lclpnet.lobby.game;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import work.lclpnet.kibu.plugin.cmd.CommandStack;
import work.lclpnet.kibu.plugin.hook.HookStack;
import work.lclpnet.kibu.plugin.scheduler.SchedulerStack;
import work.lclpnet.lobby.LobbyAPI;
import work.lclpnet.mplugins.ext.Unloadable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FinishableGameEnvironment implements GameEnvironment, GameFinisher {

    private final MinecraftServer server;
    private final Logger logger;
    private volatile List<Unloadable> closeWhenDone = null;
    private volatile HookStack hookStack;
    private volatile CommandStack commandStack;
    private volatile SchedulerStack schedulerStack;
    private GameOwner owner = null;

    public FinishableGameEnvironment(MinecraftServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    public void bind(GameOwner owner) {
        synchronized (this) {
            this.owner = owner;
        }
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
    public void closeWhenDone(Unloadable unloadable) {
        Objects.requireNonNull(unloadable);

        synchronized (this) {
            if (closeWhenDone == null) {
                closeWhenDone = new ArrayList<>();
            }

            closeWhenDone.add(unloadable);
        }
    }

    @Override
    public GameFinisher getFinisher() {
        return this;
    }

    @Override
    public void finishGame(Reason reason) {
        if (owner != null) {
            owner.detach();
        }

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

            if (closeWhenDone != null) {
                closeWhenDone.forEach(Unloadable::unload);
                closeWhenDone.clear();
            }

            if (reason != Reason.UNLOADED) {
                LobbyAPI.getInstance().enterLobbyPhase();
            }
        });
    }
}
