package work.lclpnet.game.impl;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import work.lclpnet.game.api.WorldFacade;
import work.lclpnet.game.api.WorldOptions;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.player.PlayerSpawnLocationCallback;
import work.lclpnet.kibu.hook.util.PositionRotation;
import xyz.nucleoid.fantasy.RuntimeLevelHandle;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class WorldFacadeImpl implements WorldFacade {

    private final MinecraftServer server;
    private final WorldContainer worldContainer;
    private final WorldUnloader worldUnloader;
    private WorldOptions mapOptions = null;
    private ResourceKey<Level> mapKey = null;
    private PositionRotation spawn = null;

    public WorldFacadeImpl(MinecraftServer server, WorldContainer worldContainer) {
        this.server = server;
        this.worldContainer = worldContainer;
        this.worldUnloader = new WorldUnloader(server, worldContainer);
    }

    public void init(HookRegistrar registrar) {
        registrar.registerHook(PlayerSpawnLocationCallback.HOOK, this::modifySpawnLocation);

        worldUnloader.init(registrar);
    }

    private void modifySpawnLocation(PlayerSpawnLocationCallback.LocationData data) {
        if (mapKey == null || spawn == null) return;

        ServerLevel world = this.server.getLevel(mapKey);

        if (world == null) {
            throw new IllegalStateException("World %s is not loaded".formatted(mapKey.identifier()));
        }

        data.setWorld(world);
        data.setPosition(new Vec3(spawn.x(), spawn.y(), spawn.z()));
        data.setYaw(spawn.getYaw());
        data.setPitch(spawn.getPitch());
    }

    @Override
    public void teleport(ServerPlayer player) {
        if (mapKey == null || spawn == null) return;

        ServerLevel world = this.server.getLevel(mapKey);

        if (world == null) {
            throw new IllegalStateException("World %s is not loaded".formatted(mapKey.identifier()));
        }

        player.teleportTo(world, spawn.x(), spawn.y(), spawn.z(), Set.of(), spawn.getYaw(), spawn.getPitch(), true);
    }

    @Override
    public CompletableFuture<ServerLevel> changeLevel(
            Identifier id,
            WorldOptions options,
            Function<ServerLevel, CompletableFuture<PositionRotation>> spawnGetter,
            Function<ResourceKey<Level>, CompletableFuture<RuntimeLevelHandle>> factory
    ) {
        var key = ResourceKey.create(Registries.DIMENSION, id);

        ServerLevel existingLevel = server.getLevel(key);

        if (existingLevel == null) {
            return changeToYetUnloadedLevel(options, spawnGetter, () -> factory.apply(key));
        }

        if (options.isCleanMapRequired()) {
            return worldUnloader.unloadMap(key)
                    .thenCompose(_ -> changeToYetUnloadedLevel(options, spawnGetter, () -> factory.apply(key)));
        }

        return spawnGetter.apply(existingLevel).thenCompose(spawn -> server.submit(() -> {
            onLevelReady(existingLevel, options, spawn);

            return existingLevel;
        }));
    }

    private CompletableFuture<ServerLevel> changeToYetUnloadedLevel(
            WorldOptions options,
            Function<ServerLevel, CompletableFuture<PositionRotation>> spawnGetter,
            Supplier<CompletableFuture<RuntimeLevelHandle>> handleSupplier
    ) {
        return handleSupplier.get().thenComposeAsync(handle -> {
            PositionRotation spawn = spawnGetter.apply(handle.asLevel()).join();

            return server.submit(() -> {
                // automatically unload world, if not done manually
                worldContainer.trackHandle(handle);

                onLevelReady(handle.asLevel(), options, spawn);

                return handle.asLevel();
            });
        });
    }

    private void onLevelReady(ServerLevel level, WorldOptions options, PositionRotation spawn) {
        ResourceKey<Level> oldKey = this.mapKey;
        WorldOptions oldOptions = this.mapOptions;

        ResourceKey<Level> newKey = level.dimension();

        this.mapKey = newKey;
        this.mapOptions = options;
        this.spawn = spawn;

        if (options.shouldTeleportPlayers()) {
            for (ServerPlayer player : PlayerLookup.all(server)) {
                player.teleportTo(level, spawn.x(), spawn.y(), spawn.z(), Set.of(), spawn.getYaw(), spawn.getPitch(), true);
            }
        }

        // cleanup current map if requested
        if (oldKey != null && oldOptions != null && oldOptions.shouldBeDeleted() && !newKey.equals(oldKey)) {
            worldContainer.getHandle(oldKey).ifPresent(RuntimeLevelHandle::delete);
        }
    }
}
