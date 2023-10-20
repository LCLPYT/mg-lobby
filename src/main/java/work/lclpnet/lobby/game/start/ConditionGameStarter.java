package work.lclpnet.lobby.game.start;

import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.plugin.hook.HookStack;
import work.lclpnet.lobby.activity.GameStartingActivity;
import work.lclpnet.lobby.game.api.GameEnvironment;
import work.lclpnet.lobby.game.api.GameStarter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

public class ConditionGameStarter implements GameStarter {

    private final BooleanSupplier condition;
    private final Args args;
    private final Callback onStart;
    private final GameEnvironment environment;
    private final AtomicBoolean gameStarting = new AtomicBoolean(false);
    private final AtomicBoolean gameStarted = new AtomicBoolean(false);
    private boolean paused = false;

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

        hookStack.registerHook(PlayerConnectionHooks.JOIN, this::onJoin);  // TODO find better event
        hookStack.registerHook(PlayerConnectionHooks.QUIT, this::onQuit);  // TODO find better event

        updateGameStatus();
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

        GameStartingActivity activity = lobbyArgs.createGameStartingActivity();

        args.startChildActivity(activity);

        gameStarting.set(true);
    }

    private void abortGameStart() {
        if (!gameStarting.get()) return;

        args.stopChildActivity();

        gameStarting.set(false);
    }

    private void onJoin(ServerPlayerEntity player) {
        updateGameStatus();
    }

    private void onQuit(ServerPlayerEntity player) {
        updateGameStatus();
    }
}
