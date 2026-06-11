package work.lclpnet.game.impl;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import work.lclpnet.game.api.MapOptions;
import work.lclpnet.game.api.WorldFacade;
import work.lclpnet.game.api.WorldOptions;
import work.lclpnet.game.map.GameMap;
import work.lclpnet.game.map.MapManager;
import work.lclpnet.game.map.MapUtils;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.player.PlayerSpawnLocationCallback;
import work.lclpnet.kibu.hook.util.PositionRotation;
import work.lclpnet.kibu.world.KibuLevels;
import work.lclpnet.kibu.world.mixin.MinecraftServerAccessor;
import xyz.nucleoid.fantasy.RuntimeLevelHandle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

public class WorldFacadeImpl implements WorldFacade {

    private final MinecraftServer server;
    private final MapManager mapManager;
    private final WorldContainer worldContainer;
    private final WorldUnloader worldUnloader;
    private final Logger logger;
    private WorldOptions mapOptions = null;
    private ResourceKey<Level> mapKey = null;
    private PositionRotation spawn = null;

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
    public CompletableFuture<ServerLevel> changeMap(Identifier identifier, MapOptions options) {
        var map = mapManager.getCollection().getMap(identifier);

        if (map.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Unknown map %s".formatted(identifier)));
        }

        var newKey = ResourceKey.create(Registries.DIMENSION, identifier);

        ServerLevel existingWorld = server.getLevel(newKey);

        if (existingWorld != null) {
            if (options.worldOptions().isCleanMapRequired()) {
                return worldUnloader.unloadMap(newKey)
                        .thenCompose(_ -> changeToYetUnloadedMap(map.get(), newKey, options));
            }

            return onMapLevelLoaded(map.get(), existingWorld, options);
        }

        return changeToYetUnloadedMap(map.get(), newKey, options);
    }

    @Override
    public CompletableFuture<ServerLevel> changeLevel(
            Identifier id,
            WorldOptions options,
            PositionRotation spawn,
            Supplier<RuntimeLevelHandle> handleSupplier
    ) {
        var key = ResourceKey.create(Registries.DIMENSION, id);

        ServerLevel existingLevel = server.getLevel(key);

        if (existingLevel == null) {
            return changeToYetUnloadedLevel(options, spawn, handleSupplier);
        }

        if (options.isCleanMapRequired()) {
            return worldUnloader.unloadMap(key)
                    .thenCompose(_ -> changeToYetUnloadedLevel(options, spawn, handleSupplier));
        }

        return server.submit(() -> {
            onLevelReady(existingLevel, options, spawn);

            return existingLevel;
        });
    }

    private CompletableFuture<ServerLevel> changeToYetUnloadedLevel(
            WorldOptions options,
            PositionRotation spawn,
            Supplier<RuntimeLevelHandle> handleSupplier
    ) {
        RuntimeLevelHandle handle = handleSupplier.get();

        trackLevelHandle(handle);

        return server.submit(() -> {
            onLevelReady(handle.asLevel(), options, spawn);

            return handle.asLevel();
        });
    }

    private CompletableFuture<ServerLevel> changeToYetUnloadedMap(GameMap map, ResourceKey<Level> key, MapOptions options) {
        LevelStorageSource.LevelStorageAccess session = ((MinecraftServerAccessor) server).getStorageSource();
        Path directory = session.getDimensionPath(key);

        return CompletableFuture.runAsync(() -> prepareMapFiles(map, directory))
                .thenComposeAsync(_ -> server.submit(() -> loadMap(map, key, options)).join());
    }

    private void prepareMapFiles(GameMap map, Path directory) {
        try {
            if (Files.exists(directory)) {
                FileUtils.forceDelete(directory.toFile());
            }

            mapManager.pull(map, directory);
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }

    private @NonNull CompletableFuture<ServerLevel> loadMap(GameMap map, ResourceKey<Level> key, MapOptions options) {
        var optHandle = KibuLevels.getInstance().getWorldManager(server).openPersistentLevel(key.identifier());

        RuntimeLevelHandle handle = optHandle.orElseThrow(() -> new IllegalStateException("Failed to load map"));

        trackLevelHandle(handle);

        return onMapLevelLoaded(map, handle.asLevel(), options);
    }

    private void trackLevelHandle(RuntimeLevelHandle handle) {
        // automatically unload world, if not done manually
        worldContainer.trackHandle(handle);
    }

    private CompletableFuture<ServerLevel> onMapLevelLoaded(GameMap map, ServerLevel level, MapOptions options) {
        return options.bootstrapWorld(level, map)
                .exceptionally(throwable -> {
                    logger.error("Failed to bootstrap map. Continuing without bootstrap...", throwable);
                    return null;
                })
                .thenCompose(_ -> server.submit(() -> onMapLevelBootstrapped(map, level, options)));
    }

    private ServerLevel onMapLevelBootstrapped(GameMap map, ServerLevel level, MapOptions options) {
        Vec3 pos = MapUtils.getSpawnPosition(map);
        float yaw = MapUtils.getSpawnYaw(map);
        PositionRotation spawn = new PositionRotation(pos.x(), pos.y(), pos.z(), yaw, 0f);

        onLevelReady(level, options.worldOptions(), spawn);

        return level;
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
