package work.lclpnet.lobby;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.config.json.ConfigHandler;
import work.lclpnet.lobby.service.SyncActivityManager;
import work.lclpnet.kibu.hook.HookContainer;
import work.lclpnet.kibu.hook.level.ServerWorldReadyCallback;
import work.lclpnet.kibu.hook.level.ServerWorldUnreadyCallback;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.activity.GameStartingActivity;
import work.lclpnet.lobby.activity.LobbyActivity;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.config.ExtendedConfigSerializer;
import work.lclpnet.lobby.config.LobbyConfig;
import work.lclpnet.lobby.event.ConnectionListener;
import work.lclpnet.lobby.event.RuntimeWorldListener;
import work.lclpnet.lobby.game.AsyncGameStateIo;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.lobby.game.impl.data.PathDataPackSink;
import work.lclpnet.lobby.io.LobbyWorldDownloader;
import work.lclpnet.lobby.io.ServerPropertiesAdjuster;
import work.lclpnet.lobby.service.DataPackService;
import work.lclpnet.lobby.util.LobbyTranslations;
import work.lclpnet.lobby.util.WholesomeChatManager;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyMod implements DedicatedServerModInitializer, LobbyAPI {

    public static final String ID = "mg-lobby";
    public static final Logger logger = LoggerFactory.getLogger(ID);
    private static LobbyMod instance = null;
    private final ActivityManager activityManager = new SyncActivityManager();
    private LobbyManagerImpl manager = null;
    private Translations translations = null;
    private CompletableFuture<MinecraftServer> serverFuture = null;

    @Override
    public void onInitializeServer() {
        instance = this;

        var configDir = FabricLoader.getInstance().getConfigDir().resolve(ID);
        var gameManager = new GameManager(logger, new AsyncGameStateIo(configDir.resolve("gameManagerState.dat")));

        gameManager.discoverGames();

        var loadingTranslations = new LobbyTranslations(gameManager, logger).load();

        translations = loadingTranslations.translations();
        serverFuture = new CompletableFuture<>();

        var configSerializer = new ExtendedConfigSerializer<>(LobbyConfig.FACTORY, logger);
        var configHandler = new ConfigHandler<>(configDir.resolve("config.json"), configSerializer, logger);

        manager = new LobbyManagerImpl(translations, logger, gameManager, configHandler, serverFuture);

        var hooks = new HookContainer();
        hooks.registerHooks(new ConnectionListener());
        hooks.registerHooks(new RuntimeWorldListener());

        // load config etc. (blocking)
        manager.init();

        new ServerPropertiesAdjuster(Path.of("server.properties"), manager, logger).adjust();
        new LobbyWorldDownloader(manager, logger).renewWorld();

        var dataPacksPath = Path.of(manager.getConfig().getSafeLobbyLevelName()).resolve("datapacks");

        var executor = createExecutor();

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

        ServerWorldUnreadyCallback.HOOK.register(_ -> activityManager.stop());

        logger.info("Lobby loaded.");
    }

    private @NonNull ExecutorService createExecutor() {
        var threadCounter = new AtomicInteger();

        var executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
                .name("mg-lobby-worker-" + threadCounter.getAndIncrement())
                .factory());

        ServerLifecycleEvents.SERVER_STOPPING.register(_ -> executor.shutdown());

        return executor;
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

        activityManager.startActivity(new LobbyActivity(server, logger, manager, startingBuilder, translations, activityManager));
    }

    public static LobbyMod getInstance() {
        final LobbyMod ret = instance;
        if (instance == null) throw new IllegalStateException("Lobby mod not loaded");
        return ret;
    }

    public static @Nullable LobbyMod optInstance() {
        return instance;
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }
}