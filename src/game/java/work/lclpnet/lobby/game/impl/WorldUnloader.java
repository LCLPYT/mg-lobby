package work.lclpnet.lobby.game.impl;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import work.lclpnet.kibu.hook.ServerTickHooks;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorldUnloader {

    private final MinecraftServer server;
    private final WorldContainer worldContainer;
    private final List<Task> tasks = new ArrayList<>();

    public WorldUnloader(MinecraftServer server, WorldContainer worldContainer) {
        this.server = server;
        this.worldContainer = worldContainer;
    }

    public void init(HookRegistrar registrar) {
        registrar.registerHook(ServerTickHooks.START_SERVER_TICK, this::tick);
    }

    private void tick(MinecraftServer server) {
        synchronized (this) {
            tasks.removeIf(this::taskIsDone);
        }
    }

    private boolean taskIsDone(Task task) {
        if (server.getWorld(task.world()) != null) {
            return false;
        }

        task.future().complete(null);
        return true;
    }

    public CompletableFuture<Void> unloadMap(RegistryKey<World> key) {
        var handle = worldContainer.getHandle(key);

        if (handle.isEmpty()) {
            var error = new IllegalStateException("World is not managed by this container");
            return CompletableFuture.failedFuture(error);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        handle.get().delete();

        synchronized (this) {
            tasks.add(new Task(key, future));
        }

        return future;
    }

    private record Task(RegistryKey<World> world, CompletableFuture<Void> future) {}
}
