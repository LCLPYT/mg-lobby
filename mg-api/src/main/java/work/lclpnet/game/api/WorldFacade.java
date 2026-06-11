package work.lclpnet.game.api;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import work.lclpnet.kibu.hook.util.PositionRotation;
import xyz.nucleoid.fantasy.RuntimeLevelHandle;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface WorldFacade {

    /**
     * Changes the current map.
     * If the new map is not yet loaded, it will be loaded first.
     * All players will be moved to the new map by default.
     * Newly joining players will be moved to the new map as well.
     * @param identifier The map id.
     * @param options The map options to specify loading behavior.
     * @return A future of the loaded map level.
     */
    CompletableFuture<ServerLevel> changeMap(Identifier identifier, MapOptions options);

    /**
     * Changes the current level.
     * If the new level is not yet loaded, it will be loaded first.
     * All players will be moved to the new level by default.
     * Newly joining players will be moved to the new level as well.
     * @param id The level id.
     * @param options The level options to specify loading behavior.
     * @param spawn The new spawn position to set as default spawn.
     * @param handleSupplier The factory to create the level, if needed.
     * @return A future of the loaded level.
     */
    CompletableFuture<ServerLevel> changeLevel(Identifier id, WorldOptions options, PositionRotation spawn, Supplier<RuntimeLevelHandle> handleSupplier);

    /**
     * Teleport a player to the current level.
     * If no level is currently loaded, nothing happens.
     * @param player The player to teleport.
     */
    void teleport(ServerPlayer player);

    default CompletableFuture<ServerLevel> changeMap(Identifier identifier) {
        return changeMap(identifier, MapOptions.createSimple(WorldOptions.TEMPORARY));
    }
}
