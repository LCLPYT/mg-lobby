package work.lclpnet.game;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameApiInit implements ModInitializer {

    public static final String MOD_ID = "mg-api";

    private static final Logger logger = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        logger.info("Initialized");
    }
}
