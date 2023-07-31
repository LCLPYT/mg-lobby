package work.lclpnet.lobby.game.map;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import work.lclpnet.lobby.io.copy.WorldCopier;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class MapManager {

    private final MapCollection mapCollection;

    public MapManager(MapRepository mapRepository, Logger logger) {
        this(new MapCollection(mapRepository, logger));
    }

    public MapManager(MapCollection mapCollection) {
        this.mapCollection = mapCollection;
    }

    public MapCollection getMapCollection() {
        return mapCollection;
    }

    /**
     * Pulls the world save of a {@link GameMap} into a directory.
     * @param map The map
     * @param directory The target directory
     * @throws IOException If there was an IO error
     */
    public Path pull(GameMap map, Path directory) throws IOException {
        URI source = mapCollection.getWorldSource(map);
        Path target = directory.resolve(flattenIdentifier(map.getIdentifier()));

        WorldCopier.get(source).copyTo(target);

        return target;
    }

    private String flattenIdentifier(Identifier identifier) {
        String[] parts = identifier.getPath().split("/");
        return parts[parts.length - 1];
    }
}
