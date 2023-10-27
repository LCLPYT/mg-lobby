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
    private MapOptions mapOptions = null;
    private RegistryKey<World> mapKey = null;
    private Vec3d spawn = null;

    public WorldFacadeImpl(MinecraftServer server, MapManager mapManager, WorldContainer worldContainer) {
        this.server = server;
        this.mapManager = mapManager;
        this.worldContainer = worldContainer;
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
    }

    @Override
    public void teleport(ServerPlayerEntity player) {
        if (mapKey == null || spawn == null) return;

        ServerWorld world = this.server.getWorld(mapKey);

        if (world == null) {
            throw new IllegalStateException("World %s is not loaded".formatted(mapKey.getValue()));
        }

        player.teleport(world, spawn.getX(), spawn.getY(), spawn.getZ(), 0F, 0F);
    }

    @Override
    public CompletableFuture<ServerWorld> changeMap(Identifier identifier, MapOptions options) {
        var map = mapManager.getMapCollection().getMap(identifier);

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

            return server.submit(() -> onWorldLoaded(map.get(), newKey, existingWorld, options));
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
        }).thenCompose(nil -> server.submit(() -> {
            var optHandle = KibuWorlds.getInstance().getWorldManager(server).openPersistentWorld(newKey.getValue());

            RuntimeWorldHandle handle = optHandle.orElseThrow(() -> new IllegalStateException("Failed to load map"));

            worldContainer.trackHandle(handle);  // automatically unload world, if not done manually

            ServerWorld world = handle.asWorld();

            onWorldLoaded(map, newKey, world, options);
            return world;
        }));
    }

    private ServerWorld onWorldLoaded(GameMap map, RegistryKey<World> newKey, ServerWorld world, MapOptions options) {
        RegistryKey<World> oldKey = this.mapKey;
        MapOptions oldOptions = this.mapOptions;

        this.mapKey = newKey;
        this.mapOptions = options;
        this.spawn = MapUtils.getSpawnPosition(map);

        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            player.teleport(world, spawn.getX(), spawn.getY(), spawn.getZ(), Set.of(), 0, 0);
        }

        // cleanup current map if requested
        if (oldKey != null && oldOptions != null && oldOptions.shouldBeDeleted() && !newKey.equals(oldKey)) {
            worldContainer.getHandle(oldKey).ifPresent(RuntimeWorldHandle::delete);
        }

        return world;
    }
}
