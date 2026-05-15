package work.lclpnet.lobby.util;

import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.NonNull;
import work.lclpnet.game.api.GameConfig;
import work.lclpnet.game.api.GameContext;
import work.lclpnet.kibu.translate.Translations;

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
    public @NonNull MinecraftServer getServer() {
        return server;
    }

    @Override
    public @NonNull GameConfig getGameConfig() {
        return gameConfig;
    }

    @Override
    public @NonNull Translations getTranslations() {
        return translations;
    }
}
