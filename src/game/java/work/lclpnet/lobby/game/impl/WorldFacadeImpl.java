package work.lclpnet.lobby.game.impl;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import work.lclpnet.kibu.hook.player.PlayerSpawnLocationCallback;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
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
    private RegistryKey<World> mapKey = null;
    private Vec3d spawn = null;
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

        ServerWorld world = this.server.getWorld(mapKey);

        if (world == null) {
            throw new IllegalStateException("World %s is not loaded".formatted(mapKey.getValue()));
        }

        data.setWorld(world);
        data.setPosition(spawn);
        data.setYaw(yaw);
    }

    @Override
    public void teleport(ServerPlayerEntity player) {
        if (mapKey == null || spawn == null) return;

        ServerWorld world = this.server.getWorld(mapKey);

        if (world == null) {
            throw new IllegalStateException("World %s is not loaded".formatted(mapKey.getValue()));
        }

        player.teleport(world, spawn.getX(), spawn.getY(), spawn.getZ(), yaw, 0F);
    }

    @Override
    public CompletableFuture<ServerWorld> changeMap(Identifier identifier, MapOptions options) {
        var map = mapManager.getCollection().getMap(identifier);

        if (map.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Unknown map %s".formatted(identifier)));
        }

        var newKey = RegistryKey.of(RegistryKeys.WORLD, identifier);

        ServerWorld existingWorld = server.getWorld(newKey);

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

    private CompletableFuture<ServerWorld> changeToYetUnloadedMap(GameMap map, RegistryKey<World> newKey, MapOptions options) {
        LevelStorage.Session session = ((MinecraftServerAccessor) server).getSession();
        Path directory = session.getWorldDirectory(newKey);

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
            var optHandle = KibuWorlds.getInstance().getWorldManager(server).openPersistentWorld(newKey.getValue());

            RuntimeWorldHandle handle = optHandle.orElseThrow(() -> new IllegalStateException("Failed to load map"));

            worldContainer.trackHandle(handle);  // automatically unload world, if not done manually

            ServerWorld world = handle.asWorld();

            return onWorldLoaded(map, newKey, world, options);
        }).join());
    }

    private CompletableFuture<ServerWorld> onWorldLoaded(GameMap map, RegistryKey<World> newKey, ServerWorld world, MapOptions options) {
        return options.bootstrapWorld(world, map)
                .exceptionally(throwable -> {
                    logger.error("Failed to bootstrap map. Continuing without bootrap...", throwable);
                    return null;
                })
                .thenCompose(nil -> server.submit(() -> onWorldBootstrapped(map, newKey, world, options)));
    }

    private ServerWorld onWorldBootstrapped(GameMap map, RegistryKey<World> newKey, ServerWorld world, MapOptions options) {
        RegistryKey<World> oldKey = this.mapKey;
        MapOptions oldOptions = this.mapOptions;

        this.mapKey = newKey;
        this.mapOptions = options;
        this.spawn = MapUtils.getSpawnPosition(map);
        this.yaw = MapUtils.getSpawnYaw(map);

        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            player.teleport(world, spawn.getX(), spawn.getY(), spawn.getZ(), Set.of(), yaw, 0);
        }

        // cleanup current map if requested
        if (oldKey != null && oldOptions != null && oldOptions.shouldBeDeleted() && !newKey.equals(oldKey)) {
            worldContainer.getHandle(oldKey).ifPresent(RuntimeWorldHandle::delete);
        }

        return world;
    }
}
