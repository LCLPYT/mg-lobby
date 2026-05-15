package work.lclpnet.lobby.game;

import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import work.lclpnet.activity.Activity;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.game.api.GameConfig;
import work.lclpnet.game.api.GameEnvironment;
import work.lclpnet.game.api.GameFinisher;
import work.lclpnet.game.api.WorldFacade;
import work.lclpnet.game.impl.WorldContainer;
import work.lclpnet.game.impl.WorldFacadeImpl;
import work.lclpnet.game.map.MapManager;
import work.lclpnet.kibu.cmd.impl.CommandContainer;
import work.lclpnet.kibu.cmd.impl.CommandStack;
import work.lclpnet.kibu.hook.HookStack;
import work.lclpnet.kibu.scheduler.util.SchedulerStack;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.LobbyAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class FinishableGameEnvironment implements GameEnvironment, GameFinisher {

    private final MinecraftServer server;
    private final Logger logger;
    private final GameConfig gameConfig;
    private final Translations translations;
    private volatile boolean destroyed = false;
    private volatile List<Runnable> whenDone = null;
    private volatile HookStack hookStack;
    private volatile CommandStack commandStack;
    private volatile SchedulerStack schedulerStack;
    private volatile WorldFacadeImpl worldFacade;
    private WorldContainer worldContainer;

    private final ActivityManager activityManager;

    public FinishableGameEnvironment(MinecraftServer server, Logger logger, GameConfig gameConfig, Translations translations, ActivityManager activityManager) {
        this.server = server;
        this.logger = logger;
        this.gameConfig = gameConfig;
        this.translations = translations;
        this.activityManager = activityManager;
    }

    @Override
    public @NonNull MinecraftServer getServer() {
        return server;
    }

    @Override
    public @NonNull HookStack getHookStack() {
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
    public @NonNull CommandStack getCommandStack() {
        assertNotDestroyed();

        if (commandStack != null) {
            return commandStack;
        }

        synchronized (this) {
            if (commandStack == null) {
                commandStack = new CommandStack(CommandContainer::new);
            }

            return commandStack;
        }
    }

    @Override
    public @NonNull SchedulerStack getSchedulerStack() {
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
    public @NonNull WorldFacade getWorldFacade(@NonNull Supplier<MapManager> mapManagerSupplier) {
        assertNotDestroyed();

        if (worldFacade != null) {
            return worldFacade;
        }

        synchronized (this) {
            if (worldFacade != null) return worldFacade;

            worldContainer = new WorldContainer(server);
            worldFacade = new WorldFacadeImpl(server, mapManagerSupplier.get(), worldContainer, logger);
        }

        HookStack hookStack = getHookStack();

        worldFacade.init(hookStack);

        return worldFacade;
    }

    @Override
    public @NonNull GameConfig getGameConfig() {
        return gameConfig;
    }

    @Override
    public @NonNull GameFinisher getFinisher() {
        return this;
    }

    @Override
    public void whenDone(@NonNull Runnable action) {
        Objects.requireNonNull(action);

        assertNotDestroyed();

        synchronized (this) {
            if (whenDone == null) {
                whenDone = new ArrayList<>();
            }

            whenDone.add(action);
        }
    }

    @Override
    public @NonNull Translations getTranslations() {
        return translations;
    }

    private void assertNotDestroyed() {
        synchronized (this) {
            if (destroyed) {
                throw new IllegalStateException("Game environment is already destroyed");
            }
        }
    }

    @Override
    public void switchRootActivity(@NonNull Activity activity) {
        activityManager.startActivity(activity);
    }

    @Override
    public void finishGame(Reason reason) {
        server.execute(() -> {
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

                if (whenDone != null) {
                    whenDone.forEach(Runnable::run);
                    whenDone.clear();
                }
            }

            LobbyAPI.getInstance().enterLobbyPhase();
        });
    }
}
