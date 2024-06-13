package work.lclpnet.lobby.service;

import dagger.Lazy;
import org.slf4j.Logger;
import work.lclpnet.kibu.jnbt.CompoundTag;
import work.lclpnet.kibu.jnbt.ListTag;
import work.lclpnet.kibu.jnbt.StringTag;
import work.lclpnet.kibu.jnbt.Tag;
import work.lclpnet.kibu.jnbt.io.NbtIOHelper;
import work.lclpnet.lobby.config.ConfigAccess;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.lobby.game.api.Game;
import work.lclpnet.lobby.game.api.data.DataPackSink;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class DataPackService {

    private final GameManager gameManager;
    private final ConfigAccess configAccess;
    private final Lazy<DataPackSink> sink;
    private final Executor executor;
    private final Logger logger;

    @Inject
    public DataPackService(GameManager gameManager, ConfigAccess configAccess, Lazy<DataPackSink> sink, Executor executor, Logger logger) {
        this.gameManager = gameManager;
        this.configAccess = configAccess;
        this.sink = sink;
        this.executor = executor;
        this.logger = logger;
    }

    public void downloadRequired() {
        gameManager.reload();

        DataPackSink packSink = sink.get();

        gameManager.getGames().stream()
                .map(Game::getBootstrapDataPacks)
                .map(packs -> packs.downloadPacks(packSink, executor).exceptionally(err -> {
                    logger.error("Failed to download data packs", err);
                    return null;
                }))
                .toList()  // dispatch all futures, then join
                .forEach(CompletableFuture::join);

        // data packs must be enabled in order to be loaded
        Set<String> packs = packSink.getIds().stream()
                .map(path -> "file/" + path.getFileName())
                .collect(Collectors.toSet());

        try {
            enableDataPacks(packs);
        } catch (IOException e) {
            logger.error("Failed to enable data packs", e);
        }
    }

    private void enableDataPacks(Set<String> packs) throws IOException {
        String levelName = configAccess.getConfig().getSafeLobbyLevelName();

        Path path = Path.of(levelName).resolve("level.dat");

        if (!Files.isRegularFile(path)) {
            throw new FileNotFoundException(path.toAbsolutePath().toString());
        }

        Tag parsed;

        try (var in = Files.newInputStream(path)) {
            parsed = NbtIOHelper.read(in).tag();
        }

        if (!(parsed instanceof CompoundTag root) || !patchNbt(root, packs)) return;

        try (var out = Files.newOutputStream(path)) {
            NbtIOHelper.write(root, out);
        }
    }

    private static boolean patchNbt(CompoundTag root, Set<String> packs) {
        CompoundTag data = root.getCompound("Data");
        CompoundTag dataPacks = data.getCompound("DataPacks");

        ListTag enabled = dataPacks.getList("Enabled");
        ListTag disabled = dataPacks.getList("Disabled");

        boolean changed = false;

        for (String pack : packs) {
            StringTag tag = new StringTag(pack);

            if (disabled.contains(tag) || enabled.contains(tag)) continue;

            enabled.add(tag);
            changed = true;
        }

        return changed;
    }
}
