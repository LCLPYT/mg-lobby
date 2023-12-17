package work.lclpnet.lobby.service;

import work.lclpnet.pal.PalApi;

public class PalService {

    public static void configurePal() {
        PalApi.getInstance().editConfig(palConfig -> {
            palConfig.enablePlates = true;
            palConfig.enablePads = true;
            palConfig.enableElevators = true;
            palConfig.enableTeleporters = true;
        });
    }
}
