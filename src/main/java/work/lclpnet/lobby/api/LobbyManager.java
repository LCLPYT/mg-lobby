package work.lclpnet.lobby.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import work.lclpnet.lobby.config.ConfigAccess;

public interface LobbyManager extends ConfigAccess, LoggerAware {

    ServerWorld getLobbyWorld();

    Vec3d getLobbySpawn();

    void sendToLobby(ServerPlayerEntity player);
}
