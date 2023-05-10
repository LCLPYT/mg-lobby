package work.lclpnet.lobby;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.kibu.plugin.KibuPlugin;
import work.lclpnet.lobby.activity.LobbyActivity;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.event.ConnectionListener;
import work.lclpnet.lobby.io.LobbyWorldDownloader;
import work.lclpnet.lobby.io.ServerPropertiesAdjuster;
import work.lclpnet.mplugins.ext.WorldStateListener;

import java.nio.file.Path;

public class LobbyPlugin extends KibuPlugin implements WorldStateListener, LobbyAPI {

    public static final String ID = "mg-lobby";
    private static final Logger logger = LoggerFactory.getLogger(ID);
    private static LobbyPlugin instance = null;
    private final LobbyManagerImpl manager = new LobbyManagerImpl(this, logger);
    private LobbyActivity lobbyActivity;

    @Override
    public void loadKibuPlugin() {
        instance = this;

        registerHooks(new ConnectionListener());

        // load config etc.
        manager.init();

        // renew world on initial server startup
        if (getEnvironment().getServer() == null) {
            // adjust the level name in server.properties
            new ServerPropertiesAdjuster(Path.of("server.properties"), manager, logger).adjust();
            new LobbyWorldDownloader(manager).renewWorld();
        }

        logger.info("Lobby loaded.");
    }

    @Override
    public void onWorldReady() {
        lobbyActivity = new LobbyActivity(this, manager);
        lobbyActivity.start();
    }

    @Override
    public void onWorldUnready() {
        // called when the main world or the plugin is unloading
        lobbyActivity.stop();
    }

    @Override
    public LobbyManager getManager() {
        return manager;
    }

    static LobbyPlugin getInstance() {
        final LobbyPlugin ret = instance;
        if (instance == null) throw new IllegalStateException("Lobby plugin not loaded");
        return ret;
    }

    public static Identifier identifier(String path) {
        return new Identifier(ID, path);
    }
}