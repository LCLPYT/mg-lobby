package work.lclpnet.lobby.game;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class AsyncGameStateIo implements GameStateIo {

    private final GameManagerState state = new GameManagerState();
    private final Path path;

    @Inject
    public AsyncGameStateIo(@Named("gameManagerStatePath") Path path) {
        this.path = path;
    }

    @Override
    public CompletableFuture<Void> store() {
        return CompletableFuture.runAsync(() -> {
            try {
                storeSync();
            } catch (IOException e) {
                throw new RuntimeException("Failed to store state", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> restore(GameMangerLoader loader) {
        return CompletableFuture.runAsync(() -> {
            try {
                restoreSync(loader);
            } catch (IOException e) {
                throw new RuntimeException("Failed to restore state", e);
            }
        });
    }

    @NotNull
    @Override
    public GameManagerState getState() {
        return state;
    }

    private void storeSync() throws IOException {
        synchronized (this) {
            Path dir = path.getParent();

            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            try (var out = Files.newOutputStream(path)) {
                NbtIo.writeCompressed(state.toNbt(), out);
            }
        }
    }

    private void restoreSync(GameMangerLoader loader) throws IOException {
        synchronized (this) {
            if (!Files.exists(path)) return;

            NbtCompound nbt;

            try (var in = Files.newInputStream(path)) {
                nbt = NbtIo.readCompressed(in);
            }

            state.fromNbt(nbt, loader);
        }
    }
}
