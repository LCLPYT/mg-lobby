package work.lclpnet.lobby.game.start;

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

    public DefaultGameStarter(PluginContext pluginContext, HookRegistrar hookRegistrar, ActivityManager activityManager, TranslationService translations, Game game) {
        this.pluginContext = pluginContext;
        this.hookRegistrar = hookRegistrar;
        this.activityManager = activityManager;
        this.translations = translations;
        this.game = game;
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

        activityManager.startActivity(new GameStartingActivity(pluginContext, game.getConfig(), this, translations));

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
}
