package work.lclpnet.lobby.util;

import net.minecraft.server.MinecraftServer;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.game.api.GameConfig;
import work.lclpnet.lobby.game.api.GameContext;

public class LobbyGameContext implements GameContext {

    private final MinecraftServer server;
    private final GameConfig gameConfig;
    private final Translations translations;

    public LobbyGameContext(MinecraftServer server, GameConfig gameConfig, Translations translations) {
        this.server = server;
        this.gameConfig = gameConfig;
        this.translations = translations;
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public GameConfig getGameConfig() {
        return gameConfig;
    }

    @Override
    public Translations getTranslations() {
        return translations;
    }
}
