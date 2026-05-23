package work.lclpnet.lobby.game.start;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.NonNull;
import work.lclpnet.activity.component.builtin.BuiltinComponents;
import work.lclpnet.game.api.GameContext;
import work.lclpnet.game.api.GameEnvironment;
import work.lclpnet.game.api.start.GameStatusManager;
import work.lclpnet.kibu.access.entity.ServerPlayerAccess;
import work.lclpnet.kibu.hook.HookStack;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.hook.player.PlayerSpawnLocationCallback;
import work.lclpnet.kibu.scheduler.Ticks;
import work.lclpnet.kibu.scheduler.util.SchedulerStack;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.bossbar.BossBarProvider;
import work.lclpnet.kibu.translate.bossbar.TranslatedBossBar;
import work.lclpnet.kibu.translate.util.Partial;
import work.lclpnet.lobby.LobbyMod;
import work.lclpnet.lobby.activity.GameStartingActivity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameStarter implements GameStatusManager {

    private final LobbyArgs args;
    private final GameEnvironment environment;
    private final BooleanSupplier condition;
    private final Runnable onStart;
    private final Partial<GameStartingActivity, GameStarter> startingActivityPartial;
    private final AtomicBoolean gameStarting = new AtomicBoolean(false);
    private final AtomicBoolean gameStarted = new AtomicBoolean(false);
    private final int conditionCheckInterval = Ticks.seconds(20);
    private boolean paused = false;
    private Function<ServerPlayer, Component> cannotStartMessage = null;
    private TranslatedBossBar bossBar = null;

    public GameStarter(LobbyArgs args, GameEnvironment environment, BooleanSupplier condition, Runnable onStart,
                       Partial<GameStartingActivity, GameStarter> activityPartial) {
        this.args = args;
        this.environment = environment;
        this.condition = condition;
        this.onStart = onStart;
        this.startingActivityPartial = activityPartial;
    }

    public void start() {
        HookStack hookStack = environment.getHookStack();
        hookStack.push();

        hookStack.registerHook(PlayerSpawnLocationCallback.HOOK, this::onJoin);
        hookStack.registerHook(PlayerConnectionHooks.QUIT, this::onQuit);

        updateGameStatus().thenRun(this::afterStatusUpdated);

        SchedulerStack schedulerStack = environment.getSchedulerStack();
        schedulerStack.push();

        PeriodicConditionBroadcast action = new PeriodicConditionBroadcast(
                gameStarting,
                gameStarted,
                conditionCheckInterval,
                this::periodicCheck
        );

        schedulerStack.interval(action, 1, conditionCheckInterval);
    }

    public void finish() {
        gameStarting.set(false);

        if (gameStarted.get()) return;

        gameStarted.set(true);

        hideBossBar();

        unload();

        onStart.run();
    }

    public void unload() {
        environment.getHookStack().pop();
        environment.getSchedulerStack().pop();
    }

    public boolean isStarted() {
        return gameStarted.get();
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void destroy() {
        abortGameStart();
        hideBossBar();

        unload();
    }

    private void periodicCheck() {
        updateGameStatus().thenRun(this::afterStatusUpdated);
    }

    private void afterStatusUpdated() {
        if (cannotStartMessage == null || gameStarting.get() || gameStarted.get()) return;

        for (ServerPlayer player : PlayerLookup.all(environment.getServer())) {
            Component text = cannotStartMessage.apply(player);
            player.sendSystemMessage(text);

            ServerPlayerAccess.playSoundToPlayer(player, SoundEvents.CHICKEN_EGG, SoundSource.NEUTRAL, 0.4f, 1f);
        }
    }

    private CompletableFuture<Void> updateGameStatus() {
        return environment.getServer().submit(() -> {
            if (condition.getAsBoolean()) {
                initGameStart();
            } else {
                abortGameStart();
                showBossBar();
            }
        });
    }

    private void initGameStart() {
        if (gameStarting.get()) return;

        hideBossBar();

        GameStartingActivity activity = startingActivityPartial.with(this);

        args.startChildActivity(activity);

        gameStarting.set(true);
    }

    private void abortGameStart() {
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

    private void onQuit(ServerPlayer player) {
        updateGameStatus();
    }

    public void configureConditionBossBar(Partial<TranslatedBossBar, BossBarProvider> bossBarPartial, Consumer<TranslatedBossBar> action) {
        hideBossBar();
        bossBar = null;

        args.configureLobby(lobbyActivity -> {
            var bossBars = lobbyActivity.component(BuiltinComponents.BOSS_BAR);

            bossBar = bossBarPartial.with(bossBars);
            bossBar.setVisible(false);
            bossBars.showOnJoin(bossBar);

            action.accept(bossBar);
        });
    }

    @Override
    public @NonNull GameContext getContext() {
        return environment;
    }

    @Override
    public void setCannotStartMessage(Function<ServerPlayer, Component> messageFunction) {
        this.cannotStartMessage = messageFunction;
    }

    @Override
    public void setCannotStartBossBarValue(Object value) {
        Translations translations = environment.getTranslations();
        Identifier barId = LobbyMod.identifier("waiting_condition");

        configureConditionBossBar(translations.translateBossBar(barId, "lobby.game.waiting_boss_bar",
                        translations.translateText(environment.getGameConfig().titleKey())
                                .formatted(ChatFormatting.AQUA, ChatFormatting.BOLD)
                                .styled(style -> style.withItalic(false)),
                        value),
                bar -> bar.formatted(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
    }
}
