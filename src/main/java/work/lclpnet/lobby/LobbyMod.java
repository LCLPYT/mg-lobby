package work.lclpnet.lobby;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.kibu.hook.HookContainer;
import work.lclpnet.kibu.hook.level.ServerWorldReadyCallback;
import work.lclpnet.kibu.hook.level.ServerWorldUnreadyCallback;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.util.ModTranslations;
import work.lclpnet.lobby.activity.GameStartingActivity;
import work.lclpnet.lobby.activity.LobbyActivity;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.config.ExtendedConfigSerializer;
import work.lclpnet.lobby.config.LobbyConfig;
import work.lclpnet.lobby.event.ConnectionListener;
import work.lclpnet.lobby.game.AsyncGameStateIo;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.lobby.game.impl.data.PathDataPackSink;
import work.lclpnet.lobby.io.LobbyWorldDownloader;
import work.lclpnet.lobby.io.ServerPropertiesAdjuster;
import work.lclpnet.lobby.service.DataPackService;
import work.lclpnet.lobby.event.RuntimeWorldListener;
import work.lclpnet.lobby.util.WholesomeChatManager;

import net.fabricmc.loader.api.FabricLoader;
import work.lclpnet.config.json.ConfigHandler;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class LobbyMod implements DedicatedServerModInitializer, LobbyAPI {

    public static final String ID = "mg-lobby";
    public static final Logger logger = LoggerFactory.getLogger(ID);
    private static LobbyMod instance = null;
    private LobbyManagerImpl manager = null;
    private Translations translations = null;
    private CompletableFuture<MinecraftServer> serverFuture = null;

    @Override
    public void onInitializeServer() {
        instance = this;

        var loadingTranslations = ModTranslations.fromAssets(ID, logger);

        translations = loadingTranslations.translations();
        serverFuture = new CompletableFuture<>();

        var configDir = FabricLoader.getInstance().getConfigDir().resolve(ID);
        var configSerializer = new ExtendedConfigSerializer<>(LobbyConfig.FACTORY, logger);
        var configHandler = new ConfigHandler<>(configDir.resolve("config.json"), configSerializer, logger);

        GameManager gameManager = new GameManager(logger, new AsyncGameStateIo(configDir.resolve("gameManagerState.dat")));
        manager = new LobbyManagerImpl(translations, logger, gameManager, configHandler, serverFuture);

        var hooks = new HookContainer();
        hooks.registerHooks(new ConnectionListener());
        hooks.registerHooks(new RuntimeWorldListener());

        // load config etc. (blocking)
        manager.init();

        new ServerPropertiesAdjuster(Path.of("server.properties"), manager, logger).adjust();
        new LobbyWorldDownloader(manager, logger).renewWorld();

        gameManager.discoverGames();

        var dataPacksPath = Path.of(manager.getConfig().getSafeLobbyLevelName()).resolve("datapacks");
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        new DataPackService(gameManager, manager, new PathDataPackSink(dataPacksPath), executor, logger).downloadRequired();

        ServerWorldReadyCallback.HOOK.register(server -> {
            logger.info("Lobby world is ready");

            serverFuture.complete(server);

            manager.onWorldReady();

            loadingTranslations.whenLoaded().whenComplete((_, err) -> {
                if (err != null) {
                    logger.error("Failed to load mg-lobby translations", err);
                }

                var wholesomeChatManager = new WholesomeChatManager(server, translations);
                wholesomeChatManager.init(hooks);

                enterLobbyPhase();
            });
        });

        ServerWorldUnreadyCallback.HOOK.register(_ -> ActivityManager.getInstance().stop());

        logger.info("Lobby loaded.");
    }

    @Override
    public LobbyManager getManager() {
        return manager;
    }

    @Override
    public void enterLobbyPhase() {
        logger.info("Entering lobby...");

        MinecraftServer server = serverFuture.resultNow();

        GameStartingActivity.Builder startingBuilder = (game, starter, trans, lobbyArgs) ->
                new GameStartingActivity(server, logger, manager.getLobbyLevel(), game, starter, trans, lobbyArgs);

        ActivityManager.getInstance().startActivity(new LobbyActivity(server, logger, manager, startingBuilder, translations));
    }

    public static LobbyMod getInstance() {
        final LobbyMod ret = instance;
        if (instance == null) throw new IllegalStateException("Lobby mod not loaded");
        return ret;
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }
}