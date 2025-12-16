package work.lclpnet.lobby.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.config.ConfigAccess;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.game.GameManager;

public interface LobbyManager extends ConfigAccess, LoggerAware {

    ServerLevel getLobbyWorld();

    Vec3 getLobbySpawn();

    void sendToLobby(ServerPlayer player);

    Translations getTranslations();

    GameManager getGameManager();

    @NotNull LobbyWorldConfig getWorldConfig();
}
