package work.lclpnet.game.api;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import work.lclpnet.kibu.hook.util.PositionRotation;
import xyz.nucleoid.fantasy.RuntimeLevelHandle;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface WorldFacade {

    /**
     * Changes the current level.
     * If the new level is not yet loaded, it will be loaded first.
     * All players will be moved to the new level by default.
     * Newly joining players will be moved to the new level as well.
     * @param id The level id.
     * @param options The level options to specify loading behavior.
     * @param spawn The new spawn position to set as default spawn.
     * @param factory The factory to create the level, if needed.
     * @return A future of the loaded level.
     */
    CompletableFuture<ServerLevel> changeLevel(
            Identifier id,
            WorldOptions options,
            Function<ServerLevel, CompletableFuture<PositionRotation>> spawn,
            Function<ResourceKey<Level>, CompletableFuture<RuntimeLevelHandle>> factory
    );

    /**
     * Teleport a player to the current level.
     * If no level is currently loaded, nothing happens.
     * @param player The player to teleport.
     */
    void teleport(ServerPlayer player);
}
