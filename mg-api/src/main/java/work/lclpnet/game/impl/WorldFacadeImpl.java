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
import java.util.function.Function;
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
        var optMap = mapManager.getCollection().getMap(identifier);

        if (optMap.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Unknown map %s".formatted(identifier)));
        }

        GameMap map = optMap.get();

        Vec3 pos = MapUtils.getSpawnPosition(map);
        float yaw = MapUtils.getSpawnYaw(map);
        PositionRotation spawn = new PositionRotation(pos.x(), pos.y(), pos.z(), yaw, 0f);

        return changeLevel(
                identifier,
                options.worldOptions(),
                spawn,
                key -> changeToYetUnloadedMap(map, key, options)
        );
    }

    @Override
    public CompletableFuture<ServerLevel> changeLevel(
            Identifier id,
            WorldOptions options,
            PositionRotation spawn,
            Function<ResourceKey<Level>, CompletableFuture<RuntimeLevelHandle>> factory
    ) {
        var key = ResourceKey.create(Registries.DIMENSION, id);

        ServerLevel existingLevel = server.getLevel(key);

        if (existingLevel == null) {
            return changeToYetUnloadedLevel(options, spawn, () -> factory.apply(key));
        }

        if (options.isCleanMapRequired()) {
            return worldUnloader.unloadMap(key)
                    .thenCompose(_ -> changeToYetUnloadedLevel(options, spawn, () -> factory.apply(key)));
        }

        return server.submit(() -> {
            onLevelReady(existingLevel, options, spawn);

            return existingLevel;
        });
    }

    private CompletableFuture<ServerLevel> changeToYetUnloadedLevel(
            WorldOptions options,
            PositionRotation spawn,
            Supplier<CompletableFuture<RuntimeLevelHandle>> handleSupplier
    ) {
        return handleSupplier.get().thenCompose(handle -> {
            // automatically unload world, if not done manually
            worldContainer.trackHandle(handle);

            return server.submit(() -> {
                onLevelReady(handle.asLevel(), options, spawn);

                return handle.asLevel();
            });
        });
    }

    private CompletableFuture<RuntimeLevelHandle> changeToYetUnloadedMap(GameMap map, ResourceKey<Level> key, MapOptions options) {
        LevelStorageSource.LevelStorageAccess session = ((MinecraftServerAccessor) server).getStorageSource();
        Path directory = session.getDimensionPath(key);

        return CompletableFuture.runAsync(() -> prepareMapFiles(map, directory))
                .thenComposeAsync(_ -> loadMap(map, key, options));
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

    private @NonNull CompletableFuture<RuntimeLevelHandle> loadMap(GameMap map, ResourceKey<Level> key, MapOptions options) {
        return server.submit(() -> KibuLevels.getInstance()
                .getWorldManager(server)
                .openPersistentLevel(key.identifier())
                .orElseThrow(() -> new IllegalStateException("Failed to load map"))
        ).thenCompose(handle -> options.bootstrapWorld(handle.asLevel(), map)
                .exceptionally(throwable -> {
                    logger.error("Failed to bootstrap map. Continuing without bootstrap...", throwable);
                    return null;
                })
                .thenApply(_ -> handle)
        );
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
