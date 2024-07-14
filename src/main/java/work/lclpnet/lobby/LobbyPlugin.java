package work.lclpnet.lobby;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.kibu.plugin.ext.KibuPlugin;
import work.lclpnet.kibu.plugin.ext.TranslatedPlugin;
import work.lclpnet.kibu.plugin.hook.PluginLifecycleHooks;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.di.DaggerLobbyComponent;
import work.lclpnet.lobby.di.LobbyComponent;
import work.lclpnet.lobby.di.LobbyModule;
import work.lclpnet.lobby.event.ConnectionListener;
import work.lclpnet.lobby.event.RuntimeWorldListener;
import work.lclpnet.lobby.game.GameOwnerCache;
import work.lclpnet.mplugins.event.PluginBootstrapEvents;
import work.lclpnet.mplugins.ext.WorldStateListener;
import work.lclpnet.translations.loader.translation.SPITranslationLoader;
import work.lclpnet.translations.loader.translation.TranslationLoader;

public class LobbyPlugin extends KibuPlugin implements WorldStateListener, LobbyAPI, TranslatedPlugin {

    public static final String ID = "mg-lobby";
    public static final Logger logger = LoggerFactory.getLogger(ID);
    private static LobbyPlugin instance = null;
    private final GameOwnerCache gameOwnerCache = new GameOwnerCache();
    private TranslationService translationService = null;
    private LobbyManagerImpl manager = null;
    private LobbyComponent component = null;

    @Override
    public void loadKibuPlugin() {
        instance = this;

        component = DaggerLobbyComponent.builder()
                .lobbyModule(new LobbyModule(logger, translationService, this))
                .build();

        manager = component.lobbyManager();

        registerHooks(new ConnectionListener());
        registerHooks(new RuntimeWorldListener());

        // load config etc.
        manager.init();

        // renew world on initial server startup
        if (getEnvironment().getServer() == null) {
            // adjust the level name in server.properties
            component.serverPropertiesAdjuster().adjust();
            component.lobbyWorldDownloader().renewWorld();
        }

        PluginBootstrapEvents.COMPLETE.register(frame -> component.dataPackService().downloadRequired());

        logger.info("Lobby loaded.");
    }

    @Override
    public void onWorldReady() {
        manager.onWorldReady();

        // reload games after plugins were reloaded
        registerHook(PluginLifecycleHooks.RELOADED, plugins -> manager.getGameManager().reload());

        enterLobbyPhase();
    }

    @Override
    public void onWorldUnready() {
        // called when the main world or the plugin is unloading
        ActivityManager.getInstance().stop();
    }

    @Override
    public LobbyManager getManager() {
        return manager;
    }

    @Override
    public void enterLobbyPhase() {
        ActivityManager.getInstance().startActivity(component.lobbyActivity());
    }

    public static LobbyPlugin getInstance() {
        final LobbyPlugin ret = instance;
        if (instance == null) throw new IllegalStateException("Lobby plugin not loaded");
        return ret;
    }

    public static Identifier identifier(String path) {
        return Identifier.of(ID, path);
    }

    @Override
    public void injectTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public TranslationLoader createTranslationLoader() {
        return new SPITranslationLoader(getClass().getClassLoader());
    }

    @NotNull
    public GameOwnerCache getGameOwnerCache() {
        return gameOwnerCache;
    }

    public TranslationService getTranslationService() {
        return translationService;
    }
}