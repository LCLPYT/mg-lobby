package work.lclpnet.game.impl;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.ServerTickHooks;

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
        if (server.getLevel(task.world()) != null) {
            return false;
        }

        task.future().complete(null);
        return true;
    }

    public CompletableFuture<Void> unloadMap(ResourceKey<Level> key) {
        var handle = worldContainer.getHandle(key);

        if (handle.isEmpty()) {
            var error = new IllegalStateException("World %s is not managed by this container".formatted(key.identifier()));
            return CompletableFuture.failedFuture(error);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        handle.get().delete();

        synchronized (this) {
            tasks.add(new Task(key, future));
        }

        return future;
    }

    private record Task(ResourceKey<Level> world, CompletableFuture<Void> future) {}
}
