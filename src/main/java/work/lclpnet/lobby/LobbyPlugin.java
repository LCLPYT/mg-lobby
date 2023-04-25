package work.lclpnet.lobby;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.kibu.plugin.KibuPlugin;
import work.lclpnet.lobby.activity.LobbyActivity;
import work.lclpnet.lobby.event.ConnectionListener;
import work.lclpnet.lobby.io.LobbyWorldReset;
import work.lclpnet.mplugins.ext.WorldStateListener;

public class LobbyPlugin extends KibuPlugin implements WorldStateListener {

    public static final String ID = "mg-lobby";
    private static final Logger logger = LoggerFactory.getLogger(ID);
    private final LobbyManagerImpl manager = new LobbyManagerImpl(this, logger);

    @Override
    public void loadKibuPlugin() {
        registerHooks(new ConnectionListener());

        // load config etc.
        manager.init();

        // renew world on initial server startup
        if (getEnvironment().getServer() == null) {
            new LobbyWorldReset(manager).renewWorld();
        }

        logger.info("Lobby loaded.");
    }

    @Override
    public void onWorldReady() {
        new LobbyActivity(this).startActivity(this);
    }

    @Override
    public void onWorldUnready() {
        // called when the main world or the plugin is unloading
    }

    public static Identifier identifier(String path) {
        return new Identifier(ID, path);
    }
}