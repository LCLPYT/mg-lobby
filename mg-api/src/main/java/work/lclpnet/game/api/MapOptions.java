package work.lclpnet.game.api;

import net.minecraft.server.level.ServerLevel;
import work.lclpnet.game.map.GameMap;

import java.util.concurrent.CompletableFuture;

public interface MapOptions {

    WorldOptions worldOptions();

    default CompletableFuture<Void> bootstrapWorld(ServerLevel world, GameMap map) {
        return CompletableFuture.completedFuture(null);
    }

    static MapOptions createSimple(WorldOptions worldOptions) {
        return () -> worldOptions;
    }
}
