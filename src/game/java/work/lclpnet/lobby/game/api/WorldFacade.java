package work.lclpnet.lobby.game.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public interface WorldFacade {

    /**
     * Changes the current map.
     * If the new map is not yet loaded, it will be loaded first.
     * All players will be moved to the new map.
     * Newly joining players will be moved to the new map as well.
     * @param identifier The map id.
     */
    CompletableFuture<Void> changeMap(Identifier identifier, MapOptions options);

    /**
     * Teleport a player to the current map.
     * If no map is currently loaded, nothing happens.
     * @param player The player to teleport.
     */
    void teleport(ServerPlayerEntity player);

    default CompletableFuture<Void> changeMap(Identifier identifier) {
        return changeMap(identifier, MapOptions.TEMPORARY);
    }
}
