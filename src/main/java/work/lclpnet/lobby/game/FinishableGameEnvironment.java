package work.lclpnet.lobby.game;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import work.lclpnet.kibu.plugin.cmd.CommandStack;
import work.lclpnet.kibu.plugin.hook.HookStack;
import work.lclpnet.kibu.plugin.scheduler.SchedulerStack;
import work.lclpnet.lobby.LobbyAPI;
import work.lclpnet.lobby.game.api.GameEnvironment;
import work.lclpnet.lobby.game.api.GameFinisher;
import work.lclpnet.lobby.game.api.WorldFacade;
import work.lclpnet.lobby.game.impl.WorldContainer;
import work.lclpnet.lobby.game.impl.WorldFacadeImpl;
import work.lclpnet.lobby.game.map.MapManager;
import work.lclpnet.mplugins.ext.Unloadable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class FinishableGameEnvironment implements GameEnvironment, GameFinisher {

    private final MinecraftServer server;
    private final Logger logger;
    private volatile boolean destroyed = false;
    private volatile List<Unloadable> closeWhenDone = null;
    private volatile HookStack hookStack;
    private volatile CommandStack commandStack;
    private volatile SchedulerStack schedulerStack;
    private volatile WorldFacadeImpl worldFacade;
    private WorldContainer worldContainer;
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
        assertNotDestroyed();

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
        assertNotDestroyed();

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
        assertNotDestroyed();

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
    public WorldFacade getWorldFacade(Supplier<MapManager> mapManagerSupplier) {
        assertNotDestroyed();

        if (worldFacade != null) {
            return worldFacade;
        }

        synchronized (this) {
            if (worldFacade != null) return worldFacade;

            worldContainer = new WorldContainer(server);
            worldFacade = new WorldFacadeImpl(server, mapManagerSupplier.get(), worldContainer);
        }

        HookStack hookStack = getHookStack();

        worldFacade.init(hookStack);

        return worldFacade;
    }

    @Override
    public GameFinisher getFinisher() {
        return this;
    }

    @Override
    public void closeWhenDone(Unloadable unloadable) {
        Objects.requireNonNull(unloadable);

        assertNotDestroyed();

        synchronized (this) {
            if (closeWhenDone == null) {
                closeWhenDone = new ArrayList<>();
            }

            closeWhenDone.add(unloadable);
        }
    }

    private void assertNotDestroyed() {
        synchronized (this) {
            if (destroyed) {
                throw new IllegalStateException("Game environment is already destroyed");
            }
        }
    }

    @Override
    public void finishGame(Reason reason) {
        if (owner != null) {
            owner.detach();
        }

        server.submit(() -> {
            synchronized (this) {
                destroyed = true;

                if (hookStack != null) {
                    hookStack.unload();
                }

                if (commandStack != null) {
                    commandStack.unload();
                }

                if (schedulerStack != null) {
                    schedulerStack.unload();
                }

                if (worldContainer != null) {
                    worldContainer.unload();
                }

                if (closeWhenDone != null) {
                    closeWhenDone.forEach(Unloadable::unload);
                    closeWhenDone.clear();
                }
            }

            if (reason != Reason.UNLOADED) {
                LobbyAPI.getInstance().enterLobbyPhase();
            }
        });
    }
}
