package work.lclpnet.lobby.game.start;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.lobby.activity.GameStartingActivity;
import work.lclpnet.lobby.game.Game;
import work.lclpnet.lobby.game.GameEnvironment;

import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultGameStarter implements GameStarter {

    private final HookRegistrar hookRegistrar;
    private final ActivityManager activityManager;
    private final AtomicBoolean gameStarting = new AtomicBoolean(false);
    private final AtomicBoolean gameStarted = new AtomicBoolean(false);
    private final Game game;
    private final GameStartingActivity.Builder gsActivityBuilder;
    private final MinecraftServer server;
    private boolean paused = false;
    private GameEnvironment environment = null;

    @AssistedInject
    public DefaultGameStarter(HookRegistrar hookRegistrar, GameStartingActivity.Builder gsActivityBuilder,
                              @Assisted ActivityManager activityManager, @Assisted Game game,
                              MinecraftServer server) {
        this.hookRegistrar = hookRegistrar;
        this.activityManager = activityManager;
        this.game = game;
        this.gsActivityBuilder = gsActivityBuilder;
        this.server = server;
    }

    @Override
    public void init() {
        environment = () -> server;

        hookRegistrar.registerHook(PlayerConnectionHooks.JOIN, this::onJoin);
        hookRegistrar.registerHook(PlayerConnectionHooks.QUIT, this::onQuit);

        updateGameStatus();
    }

    @Override
    public void start() {
        gameStarting.set(false);

        if (gameStarted.get()) return;

        gameStarted.set(true);

        game.start(environment);
    }

    @Override
    public boolean isStarted() {
        return gameStarted.get();
    }

    private void updateGameStatus() {
        server.submit(() -> {
            if (game.canStart(environment)) {
                initGameStart();
            } else {
                abortGameStart();
            }
        });
    }

    private void initGameStart() {
        if (gameStarting.get()) return;

        GameStartingActivity activity = gsActivityBuilder.create(game.getConfig(), this);
        activityManager.startActivity(activity);

        gameStarting.set(true);
    }

    private void abortGameStart() {
        if (!gameStarting.get()) return;

        activityManager.stop();

        gameStarting.set(false);
    }

    @Override
    public void destroy() {
        abortGameStart();

        hookRegistrar.unregisterHook(PlayerConnectionHooks.JOIN, this::onJoin);
        hookRegistrar.unregisterHook(PlayerConnectionHooks.QUIT, this::onQuit);
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    private void onJoin(ServerPlayerEntity player) {
        updateGameStatus();
    }

    private void onQuit(ServerPlayerEntity player) {
        updateGameStatus();
    }

    @AssistedFactory
    public interface Factory {
        DefaultGameStarter create(ActivityManager manager, Game game);
    }
}
