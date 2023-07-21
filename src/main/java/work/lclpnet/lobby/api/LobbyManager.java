package work.lclpnet.lobby.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.lobby.config.ConfigAccess;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.game.GameManager;

public interface LobbyManager extends ConfigAccess, LoggerAware {

    ServerWorld getLobbyWorld();

    Vec3d getLobbySpawn();

    void sendToLobby(ServerPlayerEntity player);

    TranslationService getTranslationService();

    GameManager getGameManager();

    @NotNull LobbyWorldConfig getWorldConfig();
}
