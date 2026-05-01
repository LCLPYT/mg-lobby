package work.lclpnet.lobby.game.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import work.lclpnet.kibu.hook.level.ServerLevelHooks;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.fantasy.RuntimeLevelHandle;

import java.util.Map;
import java.util.Optional;

public class WorldContainer {

    private final MinecraftServer server;
    private final Map<ResourceKey<Level>, RuntimeLevelHandle> levels = new Object2ObjectOpenHashMap<>();

    public WorldContainer(MinecraftServer server) {
        this.server = server;
    }

    public void init() {
        ServerLevelHooks.UNLOAD.register(this::onLevelUnload);
    }

    public RuntimeLevelHandle createTemporaryLevel(RuntimeLevelConfig config) {
        Fantasy fantasy = Fantasy.get(server);

        RuntimeLevelHandle handle = fantasy.openTemporaryLevel(config);

        trackHandle(handle);

        return handle;
    }

    public void trackHandle(RuntimeLevelHandle handle) {
        synchronized (this) {
            levels.put(handle.getRegistryKey(), handle);
        }
    }

    private void stopTracking(ResourceKey<Level> key) {
        synchronized (this) {
            levels.remove(key);
        }
    }

    public Optional<RuntimeLevelHandle> getHandle(ResourceKey<Level> key) {
        synchronized (this) {
            return Optional.ofNullable(levels.get(key));
        }
    }

    private void onLevelUnload(MinecraftServer server, ServerLevel level) {
        if (level == null) return;

        stopTracking(level.dimension());
    }

    public synchronized void unload() {
        ServerLevelHooks.UNLOAD.unregister(this::onLevelUnload);

        synchronized (this) {
            levels.values().forEach(RuntimeLevelHandle::delete);
            levels.clear();
        }
    }
}
