package work.lclpnet.lobby.game.impl;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.player.PlayerSpawnLocationCallback;
import work.lclpnet.kibu.world.KibuWorlds;
import work.lclpnet.kibu.world.mixin.MinecraftServerAccessor;
import work.lclpnet.lobby.game.api.MapOptions;
import work.lclpnet.lobby.game.api.WorldFacade;
import work.lclpnet.lobby.game.map.GameMap;
import work.lclpnet.lobby.game.map.MapManager;
import work.lclpnet.lobby.game.map.MapUtils;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class WorldFacadeImpl implements WorldFacade {

    private final MinecraftServer server;
    private final MapManager mapManager;
    private final WorldContainer worldContainer;
    private final WorldUnloader worldUnloader;
    private final Logger logger;
    private MapOptions mapOptions = null;
    private ResourceKey<Level> mapKey = null;
    private Vec3 spawn = null;
    private float yaw = 0f;

    public WorldFacadeImpl(MinecraftServer server, MapManager mapManager, WorldContainer worldContainer, Logger logger) {
        this.server = server;
        this.mapManager = mapManager;
        this.worldContainer = worldContainer;
        this.logger = logger;
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
            throw new IllegalStateException("World %s is not loaded".formatted(mapKey.location()));
        }

        data.setWorld(world);
        data.setPosition(spawn);
        data.setYaw(yaw);
    }

    @Override
    public void teleport(ServerPlayer player) {
        if (mapKey == null || spawn == null) return;

        ServerLevel world = this.server.getLevel(mapKey);

        if (world == null) {
            throw new IllegalStateException("World %s is not loaded".formatted(mapKey.location()));
        }

        player.teleportTo(world, spawn.x(), spawn.y(), spawn.z(), Set.of(), yaw, 0F, true);
    }

    @Override
    public CompletableFuture<ServerLevel> changeMap(ResourceLocation identifier, MapOptions options) {
        var map = mapManager.getCollection().getMap(identifier);

        if (map.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Unknown map %s".formatted(identifier)));
        }

        var newKey = ResourceKey.create(Registries.DIMENSION, identifier);

        ServerLevel existingWorld = server.getLevel(newKey);

        if (existingWorld != null) {
            if (options.isCleanMapRequired()) {
                return worldUnloader.unloadMap(newKey)
                        .thenCompose(nil -> changeToYetUnloadedMap(map.get(), newKey, options));
            }

            return CompletableFuture.completedFuture(null).thenComposeAsync(nil -> server.submit(
                    () -> onWorldLoaded(map.get(), newKey, existingWorld, options)
            ).join());
        }

        return changeToYetUnloadedMap(map.get(), newKey, options);
    }

    private CompletableFuture<ServerLevel> changeToYetUnloadedMap(GameMap map, ResourceKey<Level> newKey, MapOptions options) {
        LevelStorageSource.LevelStorageAccess session = ((MinecraftServerAccessor) server).getSession();
        Path directory = session.getDimensionPath(newKey);

        return CompletableFuture.runAsync(() -> {
            try {
                if (Files.exists(directory)) {
                    FileUtils.forceDelete(directory.toFile());
                }

                mapManager.pull(map, directory);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }).thenComposeAsync(nil -> server.submit(() -> {
            var optHandle = KibuWorlds.getInstance().getWorldManager(server).openPersistentWorld(newKey.location());

            RuntimeWorldHandle handle = optHandle.orElseThrow(() -> new IllegalStateException("Failed to load map"));

            worldContainer.trackHandle(handle);  // automatically unload world, if not done manually

            ServerLevel world = handle.asWorld();

            return onWorldLoaded(map, newKey, world, options);
        }).join());
    }

    private CompletableFuture<ServerLevel> onWorldLoaded(GameMap map, ResourceKey<Level> newKey, ServerLevel world, MapOptions options) {
        return options.bootstrapWorld(world, map)
                .exceptionally(throwable -> {
                    logger.error("Failed to bootstrap map. Continuing without bootrap...", throwable);
                    return null;
                })
                .thenCompose(nil -> server.submit(() -> onWorldBootstrapped(map, newKey, world, options)));
    }

    private ServerLevel onWorldBootstrapped(GameMap map, ResourceKey<Level> newKey, ServerLevel world, MapOptions options) {
        ResourceKey<Level> oldKey = this.mapKey;
        MapOptions oldOptions = this.mapOptions;

        this.mapKey = newKey;
        this.mapOptions = options;
        this.spawn = MapUtils.getSpawnPosition(map);
        this.yaw = MapUtils.getSpawnYaw(map);

        for (ServerPlayer player : PlayerLookup.all(server)) {
            player.teleportTo(world, spawn.x(), spawn.y(), spawn.z(), Set.of(), yaw, 0, true);
        }

        // cleanup current map if requested
        if (oldKey != null && oldOptions != null && oldOptions.shouldBeDeleted() && !newKey.equals(oldKey)) {
            worldContainer.getHandle(oldKey).ifPresent(RuntimeWorldHandle::delete);
        }

        return world;
    }
}
