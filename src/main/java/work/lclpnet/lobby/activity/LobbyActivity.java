package work.lclpnet.lobby.activity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.kibu.cmd.type.CommandRegistrar;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.util.ModTranslations;
import work.lclpnet.lobby.LobbyMod;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.cmd.EndCommand;
import work.lclpnet.lobby.cmd.SetGameCommand;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.decor.GeyserManager;
import work.lclpnet.lobby.decor.KingOfLadder;
import work.lclpnet.lobby.decor.greet.GreetingDisplay;
import work.lclpnet.lobby.decor.ttt.TicTacToeManager;
import work.lclpnet.lobby.di.ActivityComponent;
import work.lclpnet.lobby.di.ActivityModule;
import work.lclpnet.lobby.game.FinishableGameEnvironment;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.lobby.game.api.Game;
import work.lclpnet.lobby.game.api.GameFactory;
import work.lclpnet.lobby.game.api.start.GameScope;
import work.lclpnet.lobby.game.impl.prot.MutableProtectionConfig;
import work.lclpnet.lobby.game.impl.prot.ProtectionTypes;
import work.lclpnet.lobby.game.start.GameStarter;
import work.lclpnet.lobby.game.start.LobbyArgs;
import work.lclpnet.lobby.game.start.LobbyGameConfigurator;
import work.lclpnet.lobby.game.util.ProtectorComponent;
import work.lclpnet.lobby.game.util.ProtectorUtils;
import work.lclpnet.lobby.service.SyncActivityManager;
import work.lclpnet.lobby.util.ResetWorldModifier;
import work.lclpnet.translations.DefaultLanguageTranslator;
import work.lclpnet.translations.loader.MultiTranslationLoader;
import work.lclpnet.translations.loader.TranslationLoader;

import javax.inject.Inject;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

import static work.lclpnet.activity.component.builtin.BuiltinComponents.*;

public class LobbyActivity extends ComponentActivity {

    private final LobbyManager lobbyManager;
    private final ActivityManager childActivity;
    private final ActivityComponent.Builder componentBuilder;
    private final GameStartingActivity.Builder startingBuilder;
    private final LobbyGameConfigurator configurator = new LobbyGameConfigurator();
    private final Translations translations;
    private final ReentrantLock gameLock = new ReentrantLock();
    private GameStarter gameStarter;
    private ResetWorldModifier worldModifier;
    private KingOfLadder kingOfLadder;
    private TicTacToeManager ticTacToeManager;
    private volatile boolean changeInProgress = false;
    private volatile Game changingToGame = null;

    @Inject
    public LobbyActivity(MinecraftServer server, Logger logger, LobbyManager lobbyManager, ActivityComponent.Builder componentBuilder,
                         GameStartingActivity.Builder startingBuilder, Translations translations) {
        super(server, logger);
        this.lobbyManager = lobbyManager;
        this.childActivity = new SyncActivityManager();
        this.componentBuilder = componentBuilder;
        this.startingBuilder = startingBuilder;
        this.translations = translations;
    }

    @Override
    protected void registerComponents(ComponentBundle components) {
        components
                .add(HOOKS)
                .add(SCHEDULER)
                .add(COMMANDS)
                .add(BOSS_BAR)
                .add(ProtectorComponent.KEY);
    }

    @Override
    public void start() {
        super.start();

        configurator.setActivity(this);

        HookRegistrar hooks = component(HOOKS).hooks();
        Scheduler scheduler = component(SCHEDULER).scheduler();
        CommandRegistrar commands = component(COMMANDS).commands();

        MinecraftServer server = getServer();

        ActivityComponent component = componentBuilder
                .activityModule(new ActivityModule(hooks, scheduler, server))
                .build();

        hooks.registerHooks(component.lobbyListener());

        GameRules gameRules = lobbyManager.getLobbyWorld().getGameRules();
        gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);

        // send every online player to the lobby
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            lobbyManager.sendToLobby(player);
        }

        worldModifier = component.worldModifier();

        // generate maze
        component.mazeGenerator().create();

        // init king of the ladder
        LobbyWorldConfig config = lobbyManager.getWorldConfig();

        if (config.kingOfLadderGoal != null) {
            kingOfLadder = component.kingOfLadder();
            hooks.registerHooks(component.kingOfLadderListener());
            scheduler.interval(kingOfLadder::tick, 6);
        }

        // init geysers
        if (config.geysers != null) {
            GeyserManager geyserManager = component.geyserManager();
            scheduler.interval(geyserManager::tick, 1);
        }

        // jump and run
        if (config.jumpAndRunStart != null) {
            hooks.registerHooks(component.jumpAndRunListener());
        }

        // init seat handler
        component.seatHandler().init();

        // tic tac toe
        ticTacToeManager = component.ticTacToeManager();
        hooks.registerHooks(component.ticTacToeListener());

        // protector
        component(ProtectorComponent.KEY).configure(this::configureProtection);

        // game stuff
        final GameManager gameManager = lobbyManager.getGameManager();

        new SetGameCommand(gameManager, this::changeGame, getLogger(), translations).register(commands);

        changeGame(gameManager.getCurrentGame());

        gameManager.addStateChangeListener(this::onGameRestored);

        GreetingDisplay greetingDisplay = component.greetingDisplay();
        greetingDisplay.show();
    }

    /**
     * Callback method that tries to change the game to the last played.
     */
    private void onGameRestored() {
        if (gameStarter != null) return;

        Game game = lobbyManager.getGameManager().getCurrentGame();

        if (game == null) {
            game = chooseRandomGame();

            if (game == null) {
                getLogger().warn("Failed to auto-start: There are no games.");
                return;
            }
        }

        final Game nextGame = game;

        getServer().execute(() -> changeGame(nextGame));
    }

    @Nullable
    private Game chooseRandomGame() {
        var games = lobbyManager.getGameManager().getGames();

        if (games.isEmpty()) return null;

        Random random = new Random();

        return games.stream()
                .skip(random.nextInt(games.size()))
                .findFirst()
                .orElse(null);
    }

    private void changeGame(@Nullable Game game) {
        // when currently changing to the same game, abort
        synchronized (this) {
            if (changeInProgress && changingToGame == game) return;

            changingToGame = game;
            changeInProgress = true;
        }

        // dispatch game change process in another thread to prevent server thread blocking
        Thread.startVirtualThread(() -> {
            // ensure atomic
            gameLock.lock();

            try {
                changeAtomicAsync(game);
            } catch (Throwable err) {
                getLogger().error("Error while changing game", err);
            } finally {
                gameLock.unlock();
            }
        });
    }

    @Blocking
    private void changeAtomicAsync(@Nullable Game game) {
        lobbyManager.getGameManager().setCurrentGame(game);

        if (game == null) return;

        GameFactory factory = game.createFactory();
        Translations translations = createGameTranslations(factory).join();

        // make sure to activate the game on the server thread
        getServer().submit(() -> activateGame(game, factory, translations)).join();

        synchronized (this) {
            changingToGame = null;
            changeInProgress = false;
        }
    }

    private void activateGame(Game game, GameFactory factory, Translations translations) {
        var environment = new FinishableGameEnvironment(getServer(), getLogger(), game.getConfig(), translations);

        var args = new LobbyArgs(childActivity, configurator);
        var scope = new Scope(getServer());

        synchronized (this) {
            if (gameStarter != null) {
                gameStarter.destroy();
            }

            gameStarter = new GameStarter(() -> game.canBePlayed(scope), args, options -> {
                new EndCommand(environment.getFinisher()).register(environment.getCommandStack());

                factory.createInstance(environment).start(options);
            }, environment);

            args.injectStartingSupplier(() -> startingBuilder.create(game, gameStarter, translations));

            gameStarter.start();
        }
    }

    private CompletableFuture<Translations> createGameTranslations(GameFactory factory) {
        TranslationLoader gameTranslationLoader = factory.createTranslationLoader();

        if (gameTranslationLoader == null) {
            // game factory doesn't provide game translations, use lobby translations
            return CompletableFuture.completedFuture(this.translations);
        }

        // the game provides a translation loader, load translations union
        var lobbyTranslationLoader = ModTranslations.assetTranslationLoader(LobbyMod.ID, getLogger());

        var loader = new MultiTranslationLoader();
        loader.addLoader(lobbyTranslationLoader);
        loader.addLoader(gameTranslationLoader);

        var translator = new DefaultLanguageTranslator(loader);

        // make sure the translations are loaded
        return translator.reload().thenApply(nil -> new Translations(translator));
    }

    @Override
    public void stop() {
        super.stop();

        configurator.setActivity(null);

        worldModifier.undo();

        if (kingOfLadder != null) {
            kingOfLadder.reset();
        }

        ticTacToeManager.reset();

        childActivity.stop();

        GameManager gameManager = lobbyManager.getGameManager();
        gameManager.removeStateChangeListener(this::onGameRestored);
    }

    private void configureProtection(MutableProtectionConfig cfg) {
        cfg.disallowAll();

        cfg.allow(ProtectionTypes.USE_BLOCK, (entity, pos) ->
                entity instanceof ServerPlayerEntity player && ticTacToeManager.isTableCenter(pos)
                        && ticTacToeManager.isPlaying(player));

        ProtectorUtils.allowCreativeOperatorBypass(cfg);
    }

    private record Scope(MinecraftServer server) implements GameScope {

        @Override
        public int playerCount() {
            return PlayerLookup.all(server).size();
        }
    }
}
