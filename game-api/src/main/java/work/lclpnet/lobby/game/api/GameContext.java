package work.lclpnet.lobby.game.api;

import net.minecraft.server.MinecraftServer;
import work.lclpnet.kibu.translate.Translations;

public interface GameContext {

    MinecraftServer getServer();

    GameConfig getGameConfig();

    Translations getTranslations();
}
