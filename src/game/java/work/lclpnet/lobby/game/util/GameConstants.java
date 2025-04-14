package work.lclpnet.lobby.game.util;

import net.fabricmc.loader.api.FabricLoader;

public class GameConstants {

    public static final boolean
            DEBUG = FabricLoader.getInstance().isDevelopmentEnvironment(),
            DEVELOPMENT = DEBUG || "true".equals(System.getenv("GAME_DEVELOPMENT_MODE"));

    private GameConstants() {}
}
