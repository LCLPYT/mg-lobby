package work.lclpnet.lobby;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.kibu.plugin.KibuPlugin;
import work.lclpnet.lobby.event.ConnectionListener;
import work.lclpnet.mplugins.ext.WorldStateListener;

public class LobbyPlugin extends KibuPlugin implements WorldStateListener {

    public static final String ID = "mg-lobby";
    private static final Logger logger = LoggerFactory.getLogger(ID);

    @Override
    public void loadKibuPlugin() {
        registerHooks(new ConnectionListener());

        logger.info("Lobby loaded.");
    }

    @Override
    public void onWorldReady() {
        // called when the main world is loaded
    }

    @Override
    public void onWorldUnready() {
        // called when the main world or the plugin is unloading
    }
}