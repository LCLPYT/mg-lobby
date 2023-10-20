package work.lclpnet.lobby.game.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import work.lclpnet.kibu.hook.world.ServerWorldHooks;
import work.lclpnet.mplugins.ext.Unloadable;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.Map;
import java.util.Optional;

public class WorldContainer implements Unloadable {

    private final MinecraftServer server;
    private final Map<RegistryKey<World>, RuntimeWorldHandle> worlds = new Object2ObjectOpenHashMap<>();

    public WorldContainer(MinecraftServer server) {
        this.server = server;
    }

    public void init() {
        ServerWorldHooks.UNLOAD.register(this::onWorldUnload);
    }

    public RuntimeWorldHandle createTemporaryWorld(RuntimeWorldConfig config) {
        Fantasy fantasy = Fantasy.get(server);

        RuntimeWorldHandle handle = fantasy.openTemporaryWorld(config);

        trackHandle(handle);

        return handle;
    }

    public void trackHandle(RuntimeWorldHandle handle) {
        synchronized (this) {
            worlds.put(handle.getRegistryKey(), handle);
        }
    }

    private void stopTracking(RegistryKey<World> key) {
        synchronized (this) {
            worlds.remove(key);
        }
    }

    public Optional<RuntimeWorldHandle> getHandle(RegistryKey<World> key) {
        synchronized (this) {
            return Optional.ofNullable(worlds.get(key));
        }
    }

    private void onWorldUnload(MinecraftServer server, ServerWorld world) {
        if (world == null) return;

        stopTracking(world.getRegistryKey());
    }

    @Override
    public synchronized void unload() {
        ServerWorldHooks.UNLOAD.unregister(this::onWorldUnload);

        synchronized (this) {
            worlds.values().forEach(RuntimeWorldHandle::delete);
            worlds.clear();
        }
    }
}
