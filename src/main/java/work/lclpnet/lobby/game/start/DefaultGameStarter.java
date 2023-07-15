package work.lclpnet.lobby.game.start;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.plugin.ext.PluginContext;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.lobby.activity.GameStartingActivity;
import work.lclpnet.lobby.game.Game;

import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultGameStarter implements GameStarter {

    private final PluginContext pluginContext;
    private final HookRegistrar hookRegistrar;
    private final ActivityManager activityManager;
    private final TranslationService translations;
    private final AtomicBoolean gameStarting = new AtomicBoolean(false);
    private final AtomicBoolean gameStarted = new AtomicBoolean(false);
    private final Game game;
    private final GameStartingActivity.Builder gsActivityBuilder;

    @AssistedInject
    public DefaultGameStarter(PluginContext pluginContext, HookRegistrar hookRegistrar, TranslationService translations,
                              @Assisted ActivityManager activityManager, @Assisted Game game,
                              GameStartingActivity.Builder gsActivityBuilder) {
        this.pluginContext = pluginContext;
        this.hookRegistrar = hookRegistrar;
        this.activityManager = activityManager;
        this.translations = translations;
        this.game = game;
        this.gsActivityBuilder = gsActivityBuilder;
    }

    @Override
    public void init() {
        hookRegistrar.registerHook(PlayerConnectionHooks.JOIN_MESSAGE, this::onJoin);
        hookRegistrar.registerHook(PlayerConnectionHooks.QUIT_MESSAGE, this::onQuit);

        updateGameStatus();
    }

    @Override
    public void start() {
        gameStarting.set(false);

        if (gameStarted.get()) return;

        gameStarted.set(true);

        game.start();
    }

    @Override
    public boolean isStarted() {
        return gameStarted.get();
    }

    private void updateGameStatus() {
        if (game.canStart()) {
            initGameStart();
        } else {
            abortGameStart();
        }
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

        hookRegistrar.unregisterHook(PlayerConnectionHooks.JOIN_MESSAGE, this::onJoin);
        hookRegistrar.unregisterHook(PlayerConnectionHooks.QUIT_MESSAGE, this::onQuit);
    }

    private Text onJoin(ServerPlayerEntity player, Text message) {
        updateGameStatus();
        return message;
    }

    private Text onQuit(ServerPlayerEntity player, Text message) {
        updateGameStatus();
        return message;
    }

    @AssistedFactory
    public interface Factory {
        DefaultGameStarter create(ActivityManager manager, Game game);
    }
}
