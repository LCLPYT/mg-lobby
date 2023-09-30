package work.lclpnet.lobby.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import work.lclpnet.kibu.hook.world.ServerWorldHooks;
import work.lclpnet.mplugins.ext.Unloadable;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.HashSet;
import java.util.Set;

public class WorldContainer implements Unloadable {

    private final MinecraftServer server;
    private final Set<RuntimeWorldHandle> worlds = new HashSet<>();

    public WorldContainer(MinecraftServer server) {
        this.server = server;
    }

    public void init() {
        ServerWorldHooks.UNLOAD.register(this::onWorldUnload);
    }

    public RuntimeWorldHandle createWorld(RuntimeWorldConfig config) {
        Fantasy fantasy = Fantasy.get(server);

        RuntimeWorldHandle handle = fantasy.openTemporaryWorld(config);

        synchronized (this) {
            worlds.add(handle);
        }

        return handle;
    }

    private void onWorldUnload(MinecraftServer server, ServerWorld world) {
        if (world == null) return;

        synchronized (this) {
            worlds.removeIf(handle -> world.equals(handle.asWorld()));
        }
    }

    @Override
    public synchronized void unload() {
        ServerWorldHooks.UNLOAD.unregister(this::onWorldUnload);

        synchronized (this) {
            worlds.forEach(RuntimeWorldHandle::delete);
            worlds.clear();
        }
    }
}
