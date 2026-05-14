package work.lclpnet.lobby.activity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.kibu.cmd.type.CommandRegistrar;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.inv.prompt.OptionPrompt;
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
import work.lclpnet.lobby.decor.jnr.JumpAndRun;
import work.lclpnet.lobby.decor.maze.LobbyMazeCreator;
import work.lclpnet.lobby.decor.seat.DefaultSeatProvider;
import work.lclpnet.lobby.decor.seat.SeatHandler;
import work.lclpnet.lobby.decor.ttt.TicTacToeManager;
import work.lclpnet.lobby.event.JumpAndRunListener;
import work.lclpnet.lobby.event.KingOfLadderListener;
import work.lclpnet.lobby.event.LobbyListener;
import work.lclpnet.lobby.event.TicTacToeListener;
import work.lclpnet.lobby.game.FinishableGameEnvironment;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.game.api.Game;
import work.lclpnet.game.api.GameFactory;
import work.lclpnet.game.api.start.GameScope;
import work.lclpnet.game.impl.prot.MutableProtectionConfig;
import work.lclpnet.game.impl.prot.ProtectionTypes;
import work.lclpnet.lobby.game.start.GameStarter;
import work.lclpnet.lobby.game.start.LobbyArgs;
import work.lclpnet.lobby.game.start.LobbyGameConfigurator;
import work.lclpnet.game.util.ProtectorComponent;
import work.lclpnet.game.util.ProtectorUtils;
import work.lclpnet.lobby.service.SyncActivityManager;
import work.lclpnet.lobby.util.LobbyPlayerStateManager;
import work.lclpnet.lobby.util.ResetWorldModifier;
import work.lclpnet.translations.DefaultLanguageTranslator;
import work.lclpnet.translations.loader.MultiTranslationLoader;
import work.lclpnet.translations.loader.TranslationLoader;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

import static work.lclpnet.activity.component.builtin.BuiltinComponents.*;

public class LobbyActivity extends ComponentActivity {

    private final LobbyManager lobbyManager;
    private final ActivityManager rootActivityManager;
    private final ActivityManager childActivity;
    private final GameStartingActivity.Builder startingBuilder;
    private final LobbyGameConfigurator configurator = new LobbyGameConfigurator();
    private final Translations translations;
    private final ReentrantLock gameLock = new ReentrantLock();
    private final LobbyPlayerStateManager playerStateManager = new LobbyPlayerStateManager();

    private GameStarter gameStarter;
    private ResetWorldModifier worldModifier;
    private KingOfLadder kingOfLadder;
    private TicTacToeManager ticTacToeManager;
    private volatile boolean changeInProgress = false;
    private volatile Game changingToGame = null;

    public LobbyActivity(MinecraftServer server, Logger logger, LobbyManager lobbyManager,
                         GameStartingActivity.Builder startingBuilder, Translations translations,
                         ActivityManager rootActivityManager) {
        super(server, logger);
        this.lobbyManager = lobbyManager;
        this.rootActivityManager = rootActivityManager;
        this.childActivity = new SyncActivityManager();
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

        playerStateManager.init(hooks);

        MinecraftServer server = getServer();
        var level = lobbyManager.getLobbyLevel();
        LobbyWorldConfig config = lobbyManager.getWorldConfig();

        GameRules gameRules = level.getGameRules();
        gameRules.set(GameRules.SHOW_ADVANCEMENT_MESSAGES, false, server);

        // send every online player to the lobby
        for (ServerPlayer player : PlayerLookup.all(server)) {
            lobbyManager.sendToLobby(player);
        }

        ResetWorldModifier resetWorldModifier = new ResetWorldModifier(level, hooks);
        worldModifier = resetWorldModifier;

        hooks.registerHooks(new LobbyListener(lobbyManager, scheduler, config));

        // generate maze
        new LobbyMazeCreator(lobbyManager, getLogger(), resetWorldModifier, level).create();

        // init king of the ladder
        if (config.kingOfLadderGoal != null) {
            kingOfLadder = new KingOfLadder(level, config, translations);
            hooks.registerHooks(new KingOfLadderListener(kingOfLadder));
            scheduler.interval(kingOfLadder::tick, 6);
        }

        // init geysers
        if (config.geysers != null) {
            GeyserManager geyserManager = new GeyserManager(level, config);
            scheduler.interval(geyserManager::tick, 1);
        }

        // jump and run
        if (config.jumpAndRunStart != null) {
            JumpAndRun jumpAndRun = new JumpAndRun(level, config, resetWorldModifier, scheduler, translations);
            hooks.registerHooks(new JumpAndRunListener(jumpAndRun));
        }

        // init seat handler
        new SeatHandler(resetWorldModifier, DefaultSeatProvider.getInstance(), hooks).init();

        // tic-tac-toe
        ticTacToeManager = new TicTacToeManager(config, translations, scheduler, level, resetWorldModifier);
        hooks.registerHooks(new TicTacToeListener(ticTacToeManager));

        // protector
        component(ProtectorComponent.KEY).configure(this::configureProtection);

        // game stuff
        final GameManager gameManager = lobbyManager.getGameManager();

        new SetGameCommand(gameManager, this::changeGame, getLogger(), translations).register(commands);

        changeGame(gameManager.getCurrentGame());

        gameManager.addStateChangeListener(this::onGameRestored);

        new GreetingDisplay(config, resetWorldModifier, level).show();

        level.getWaypointManager().breakAllConnections();

        initPlayerItems(hooks, level);
    }

    private void initPlayerItems(HookRegistrar hooks, ServerLevel level) {
        hooks.registerHook(PlayerConnectionHooks.JOIN, this::giveItems);
        PlayerLookup.level(level).forEach(this::giveItems);
    }

    private void giveItems(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        var state = playerStateManager.getOrCreate(player);

        if (Commands.LEVEL_GAMEMASTERS.check(player.level().getServer().getProfilePermissions(player.nameAndId()))) {
            int gameSlot = state.getFreeSlot(8);
            inventory.setItem(gameSlot, getGameSelectorStack(player));
            state.setInteractable(gameSlot, this::openGameSelector);
        }
    }

    private ItemStack getGameSelectorStack(ServerPlayer player) {
        var stack = new ItemStack(Items.COMPASS);

        stack.set(DataComponents.ITEM_NAME, translations.translateText(player, "lobby.item.select_game")
                .formatted(ChatFormatting.GOLD));

        return stack;
    }

    private void openGameSelector(ServerPlayer player) {
        var games = new ArrayList<>(lobbyManager.getGameManager().getGames());
        var title = translations.translateText(player, "lobby.item.select_game");

        OptionPrompt.open(player, title, games, game -> {
            var icon = game.getConfig().icon().copy();

            icon.set(DataComponents.ITEM_NAME, translations
                    .translateText(player, game.getConfig().titleKey())
                    .formatted(ChatFormatting.AQUA));

            return icon;
        }).thenAccept(selected -> selected.ifPresent(
                game -> getServer().execute(() -> changeGame(game))
        ));
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
        var environment = new FinishableGameEnvironment(getServer(), getLogger(), game.getConfig(), translations, rootActivityManager);

        var args = new LobbyArgs(childActivity, configurator, playerStateManager);
        var scope = new Scope(getServer());

        synchronized (this) {
            if (gameStarter != null) {
                gameStarter.destroy();
            }

            gameStarter = new GameStarter(() -> game.canBePlayed(scope), args, options -> {
                new EndCommand(environment.getFinisher()).register(environment.getCommandStack());

                factory.createInstance(environment).start(options);
            }, environment);

            args.injectStartingSupplier(() -> startingBuilder.create(game, gameStarter, translations, args));

            game.configureStatusManager(gameStarter);

            gameStarter.start();
        }

        playerStateManager.reset();

        for (ServerPlayer player : PlayerLookup.all(getServer())) {
            player.getInventory().clearContent();
            giveItems(player);
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
        return translator.reload().thenApply(_ -> new Translations(translator));
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

        ProtectionTypes.USE_BLOCK.allow(cfg, (entity, pos) ->
                entity instanceof ServerPlayer player && ticTacToeManager.isTableCenter(pos)
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
