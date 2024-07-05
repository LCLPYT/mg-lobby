package work.lclpnet.lobby.game.map;

import java.io.IOException;
import java.nio.file.Path;

public interface MapFetcher {

    /**
     * Pulls the world save of a {@link GameMap} into a directory.
     * @param map The map
     * @param target The target directory
     * @throws IOException If there was an IO error
     */
    void pull(GameMap map, Path target) throws IOException;
}
