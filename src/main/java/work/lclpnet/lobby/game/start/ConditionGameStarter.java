package work.lclpnet.lobby.game.start;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import work.lclpnet.activity.component.builtin.BuiltinComponents;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.hook.player.PlayerSpawnLocationCallback;
import work.lclpnet.kibu.plugin.hook.HookStack;
import work.lclpnet.kibu.plugin.scheduler.SchedulerStack;
import work.lclpnet.kibu.scheduler.Ticks;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.kibu.translate.bossbar.BossBarProvider;
import work.lclpnet.kibu.translate.bossbar.TranslatedBossBar;
import work.lclpnet.kibu.translate.text.FormatWrapper;
import work.lclpnet.kibu.translate.util.Partial;
import work.lclpnet.lobby.LobbyPlugin;
import work.lclpnet.lobby.activity.GameStartingActivity;
import work.lclpnet.lobby.game.api.GameEnvironment;
import work.lclpnet.lobby.game.api.GameStarter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConditionGameStarter implements GameStarter {

    private final BooleanSupplier condition;
    private final Args args;
    private final Callback onStart;
    private final GameEnvironment environment;
    private final AtomicBoolean gameStarting = new AtomicBoolean(false);
    private final AtomicBoolean gameStarted = new AtomicBoolean(false);
    private boolean paused = false;
    private int conditionCheckInterval = Ticks.seconds(20);
    private Function<ServerPlayerEntity, Text> conditionMessage = null;
    private TranslatedBossBar bossBar = null;

    public ConditionGameStarter(BooleanSupplier condition, Args args, Callback onStart, GameEnvironment environment) {
        this.condition = condition;
        this.args = args;
        this.onStart = onStart;
        this.environment = environment;
    }

    @Override
    public void start() {
        HookStack hookStack = environment.getHookStack();
        hookStack.push();

        hookStack.registerHook(PlayerSpawnLocationCallback.HOOK, this::onJoin);
        hookStack.registerHook(PlayerConnectionHooks.QUIT, this::onQuit);

        updateGameStatus();

        SchedulerStack schedulerStack = environment.getSchedulerStack();
        schedulerStack.push();

        schedulerStack.interval(new PeriodicConditionBroadcast(gameStarting, gameStarted, conditionCheckInterval,
                this::periodicCheck), 1);
    }

    @Override
    public void finish() {
        gameStarting.set(false);

        if (gameStarted.get()) return;

        gameStarted.set(true);

        onStart.start();
    }

    @Override
    public boolean isStarted() {
        return gameStarted.get();
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void destroy() {
        abortGameStart();

        environment.getHookStack().pop();
        environment.getSchedulerStack().pop();
    }

    private void periodicCheck() {
        updateGameStatus();

        if (conditionMessage == null || gameStarting.get() || gameStarted.get()) return;

        for (ServerPlayerEntity player : PlayerLookup.all(environment.getServer())) {
            Text text = conditionMessage.apply(player);
            player.sendMessage(text);

            player.playSound(SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.NEUTRAL, 0.4f, 1f);
        }
    }

    private void updateGameStatus() {
        environment.getServer().submit(() -> {
            if (condition.getAsBoolean()) {
                initGameStart();
            } else {
                abortGameStart();
            }
        });
    }

    private void initGameStart() {
        if (gameStarting.get()) return;

        if (!(args instanceof LobbyArgs lobbyArgs)) {
            throw new RuntimeException("Expected argument type of " + LobbyArgs.class.getName());
        }

        hideBossBar();

        GameStartingActivity activity = lobbyArgs.createGameStartingActivity();

        args.startChildActivity(activity);

        gameStarting.set(true);
    }

    private void abortGameStart() {
        if (!gameStarting.get()) {
            showBossBar();
            return;
        }

        args.stopChildActivity();

        gameStarting.set(false);
    }

    private void showBossBar() {
        if (bossBar == null) return;

        bossBar.setVisible(true);

        PlayerLookup.all(environment.getServer())
                .forEach(player -> bossBar.addPlayer(player));
    }

    private void hideBossBar() {
        if (bossBar == null) return;

        bossBar.setVisible(false);

        PlayerLookup.all(environment.getServer())
                .forEach(player -> bossBar.removePlayer(player));
    }

    private void onJoin(PlayerSpawnLocationCallback.LocationData data) {
        if (!data.isJoin()) return;

        updateGameStatus();
    }

    private void onQuit(ServerPlayerEntity player) {
        updateGameStatus();
    }

    public void setConditionCheckInterval(int conditionCheckInterval) {
        this.conditionCheckInterval = conditionCheckInterval;
    }

    public void setConditionMessage(Function<ServerPlayerEntity, Text> conditionText) {
        this.conditionMessage = conditionText;
    }

    public void setConditionBossBarValue(Object value) {
        TranslationService translations = LobbyPlugin.getInstance().getTranslationService();
        Identifier barId = LobbyPlugin.identifier("waiting_condition");

        configureConditionBossBar(translations.translateBossBar(barId, "lobby.game.waiting_boss_bar",
                        FormatWrapper.styled(environment.getGameConfig().title(), Formatting.AQUA, Formatting.BOLD)
                                .styled(style -> style.withItalic(false)),
                        value),
                bar -> bar.formatted(Formatting.YELLOW, Formatting.ITALIC));
    }

    public void configureConditionBossBar(Partial<TranslatedBossBar, BossBarProvider> bossBarPartial, Consumer<TranslatedBossBar> action) {
        hideBossBar();

        if (!(args instanceof LobbyArgs lobbyArgs)) {
            throw new RuntimeException("Expected argument type of " + LobbyArgs.class.getName());
        }

        lobbyArgs.configureLobby(lobbyActivity -> {
            var bossBars = lobbyActivity.component(BuiltinComponents.BOSS_BAR);

            bossBar = bossBarPartial.with(bossBars);
            bossBar.setVisible(false);
            bossBars.showOnJoin(bossBar);

            action.accept(bossBar);
        });
    }
}
