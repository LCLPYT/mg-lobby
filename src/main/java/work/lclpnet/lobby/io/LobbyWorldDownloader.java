package work.lclpnet.lobby.io;

import work.lclpnet.lobby.config.ConfigAccess;
import work.lclpnet.lobby.io.copy.WorldCopier;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class LobbyWorldDownloader {

    private final Path lobbyDir;
    private final ConfigAccess configAccess;

    public LobbyWorldDownloader(ConfigAccess configAccess) {
        this(Path.of(configAccess.getConfig().getSafeLobbyLevelName()), configAccess);
    }

    public LobbyWorldDownloader(Path lobbyDir, ConfigAccess configAccess) {
        this.lobbyDir = lobbyDir;
        this.configAccess = configAccess;
    }

    public void renewWorld() {
        if (Files.exists(lobbyDir)) {
            // delete lobby directory recursively
            try (var files = Files.walk(lobbyDir)) {
                var iterator = files
                        .sorted(Comparator.reverseOrder())
                        .iterator();

                while (iterator.hasNext()) {
                    Path path = iterator.next();
                    Files.delete(path);
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not cleanup old lobby file");
            }
        }

        URI worldSource = configAccess.getConfig().lobbySource;
        WorldCopier copier = WorldCopier.get(worldSource);

        try {
            copier.copyTo(lobbyDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy lobby", e);
        }
    }
}
