package work.lclpnet.game.api;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

public interface WorldFacade {

    /**
     * Changes the current map.
     * If the new map is not yet loaded, it will be loaded first.
     * All players will be moved to the new map.
     * Newly joining players will be moved to the new map as well.
     * @param identifier The map id.
     */
    CompletableFuture<ServerLevel> changeMap(Identifier identifier, MapOptions options);

    /**
     * Teleport a player to the current map.
     * If no map is currently loaded, nothing happens.
     * @param player The player to teleport.
     */
    void teleport(ServerPlayer player);

    default CompletableFuture<ServerLevel> changeMap(Identifier identifier) {
        return changeMap(identifier, MapOptions.TEMPORARY);
    }
}
