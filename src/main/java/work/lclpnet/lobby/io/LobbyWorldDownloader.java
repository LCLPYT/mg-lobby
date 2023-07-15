package work.lclpnet.lobby.io;

import work.lclpnet.kibu.jnbt.CompoundTag;
import work.lclpnet.kibu.jnbt.NBTConstants;
import work.lclpnet.kibu.jnbt.Tag;
import work.lclpnet.kibu.jnbt.io.NbtIOHelper;
import work.lclpnet.lobby.config.ConfigAccess;
import work.lclpnet.lobby.io.copy.WorldCopier;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class LobbyWorldDownloader {

    private final Path lobbyDir;
    private final ConfigAccess configAccess;

    @Inject
    public LobbyWorldDownloader(ConfigAccess configAccess) {
        this(Path.of(configAccess.getConfig().getSafeLobbyLevelName()), configAccess);
    }

    public LobbyWorldDownloader(Path lobbyDir, ConfigAccess configAccess) {
        this.lobbyDir = lobbyDir;
        this.configAccess = configAccess;
    }

    public void renewWorld() {
        try {
            removeIfExists(lobbyDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not cleanup old lobby file", e);
        }

        URI worldSource = configAccess.getConfig().lobbySource;
        WorldCopier copier = WorldCopier.get(worldSource);

        try {
            copier.copyTo(lobbyDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy lobby", e);
        }

        try {
            patchWorld();
        } catch (IOException e) {
            throw new RuntimeException("Failed to patch lobby", e);
        }
    }

    private void patchWorld() throws IOException {
        removeIfExists(lobbyDir.resolve("advancements"));
        removeIfExists(lobbyDir.resolve("playerdata"));
        removeIfExists(lobbyDir.resolve("stats"));
        removeIfExists(lobbyDir.resolve("icon.png"));
        removeIfExists(lobbyDir.resolve("level.dat_old"));
        removeIfExists(lobbyDir.resolve("session.lock"));

        Path levelDataPath = lobbyDir.resolve("level.dat");
        if (Files.isRegularFile(levelDataPath)) {
            patchLevelDat(levelDataPath);
        }
    }

    private void patchLevelDat(Path levelDataPath) throws IOException {
        Tag parsed;
        try (var in = Files.newInputStream(levelDataPath)) {
            parsed = NbtIOHelper.read(in).tag();
        }

        if (!(parsed instanceof CompoundTag root)) return;

        if (!root.contains("Data", NBTConstants.TYPE_COMPOUND)) return;

        CompoundTag data = root.getCompound("Data");
        data.putString("LevelName", lobbyDir.getFileName().toString());

        try (var out = Files.newOutputStream(levelDataPath)) {
            NbtIOHelper.write(root, out);
        }
    }

    private static void removeIfExists(Path path) throws IOException {
        if (!Files.exists(path)) return;

        if (!Files.isDirectory(path)) {
            // regular file
            Files.delete(path);
            return;
        }

        // delete directory recursively
        try (var files = Files.walk(path)) {
            var iterator = files
                    .sorted(Comparator.reverseOrder())
                    .iterator();

            while (iterator.hasNext()) {
                Path p = iterator.next();
                Files.delete(p);
            }
        }
    }
}
