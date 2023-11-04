package work.lclpnet.lobby.game.api;

import net.minecraft.server.world.ServerWorld;
import work.lclpnet.lobby.game.map.GameMap;

import java.util.concurrent.CompletableFuture;

public interface MapOptions {

    /**
     * Open a clean new map and delete it after it was switched.
     */
    MapOptions TEMPORARY = new MapOptions() {
        @Override
        public boolean shouldBeDeleted() {
            return true;
        }

        @Override
        public boolean isCleanMapRequired() {
            return true;
        }
    };

    /**
     * Open a reusable map will not be deleted after it was switched.
     * When trying to open the map again, the old loaded instance will be reused.
     * However, all maps are deleted after the game ends.
     */
    MapOptions REUSABLE = new MapOptions() {
        @Override
        public boolean shouldBeDeleted() {
            return false;
        }

        @Override
        public boolean isCleanMapRequired() {
            return false;
        }
    };

    /**
     * @return Whether the map should be deleted after it was switched for another.
     */
    boolean shouldBeDeleted();

    /**
     * @return Whether an existing instance of the map should be deleted first before loading a clean version.
     */
    boolean isCleanMapRequired();

    default CompletableFuture<Void> bootstrapWorld(ServerWorld world, GameMap map) {
        return CompletableFuture.completedFuture(null);
    }
}
