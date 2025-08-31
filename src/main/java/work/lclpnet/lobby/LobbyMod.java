package work.lclpnet.lobby;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.kibu.hook.HookContainer;
import work.lclpnet.kibu.hook.world.ServerWorldReadyCallback;
import work.lclpnet.kibu.hook.world.ServerWorldUnreadyCallback;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.util.ModTranslations;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.di.DaggerLobbyComponent;
import work.lclpnet.lobby.di.LobbyComponent;
import work.lclpnet.lobby.di.LobbyModule;
import work.lclpnet.lobby.event.ConnectionListener;
import work.lclpnet.lobby.event.RuntimeWorldListener;

import java.util.concurrent.CompletableFuture;

public class LobbyMod implements DedicatedServerModInitializer, LobbyAPI {

    public static final String ID = "mg-lobby";
    public static final Logger logger = LoggerFactory.getLogger(ID);
    private static LobbyMod instance = null;
    private LobbyManagerImpl manager = null;
    private LobbyComponent component = null;

    @Override
    public void onInitializeServer() {
        instance = this;

        var loadingTranslations = ModTranslations.fromAssets(ID, logger);

        Translations translations = loadingTranslations.translations();

        var serverFuture = new CompletableFuture<MinecraftServer>();

        component = DaggerLobbyComponent.builder()
                .lobbyModule(new LobbyModule(logger, translations, serverFuture))
                .build();

        manager = component.lobbyManager();

        var hooks = new HookContainer();
        hooks.registerHooks(new ConnectionListener());
        hooks.registerHooks(new RuntimeWorldListener());

        // load config etc. (blocking)
        manager.init();

        // adjust the level name in server.properties (blocking)
        component.serverPropertiesAdjuster().adjust();

        // renew world on initial server startup (blocking)
        component.lobbyWorldDownloader().renewWorld();

        // load games
        component.lobbyManager().getGameManager().discoverGames();

        // download required data packs (blocking)
        component.dataPackService().downloadRequired();

        ServerWorldReadyCallback.HOOK.register(server -> {
            logger.info("Lobby world is ready");

            serverFuture.complete(server);

            manager.onWorldReady();

            loadingTranslations.whenLoaded().whenComplete((nil, err) -> {
                if (err != null) {
                    logger.error("Failed to load mg-lobby translations", err);
                }

                enterLobbyPhase();
            });
        });

        ServerWorldUnreadyCallback.HOOK.register(server -> ActivityManager.getInstance().stop());

        logger.info("Lobby loaded.");
    }

    @Override
    public LobbyManager getManager() {
        return manager;
    }

    @Override
    public void enterLobbyPhase() {
        logger.info("Entering lobby...");

        ActivityManager.getInstance().startActivity(component.lobbyActivity());
    }

    public static LobbyMod getInstance() {
        final LobbyMod ret = instance;
        if (instance == null) throw new IllegalStateException("Lobby mod not loaded");
        return ret;
    }

    public static Identifier identifier(String path) {
        return Identifier.of(ID, path);
    }
}