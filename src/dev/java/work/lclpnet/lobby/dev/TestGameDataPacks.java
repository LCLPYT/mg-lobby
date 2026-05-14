package work.lclpnet.lobby.dev;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import work.lclpnet.lobby.game.api.data.DataPackSink;
import work.lclpnet.lobby.game.api.data.GameDataPacks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TestGameDataPacks implements GameDataPacks {

    @Override
    public @NonNull CompletableFuture<Void> downloadPacks(@NotNull DataPackSink sink, @NotNull Executor executor) {
        // offer all required data packs asynchronously to the sink
        // in this case, we want to add a single data pack from the resources folder

        return CompletableFuture.runAsync(() -> {
            InputStream input = getClass().getResourceAsStream("/datapacks/as_ruins.zip");

            if (input == null) return;

            try (input) {
                sink.offer(Path.of("as_ruins.zip"), input);
            } catch (IOException e) {
                throw new RuntimeException("Failed to offer data pack", e);
            }
        }, executor);
    }
}
