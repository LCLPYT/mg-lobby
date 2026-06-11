package work.lclpnet.game.map;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

public interface MapLookup {

    Collection<GameMap> getMaps(MapDescriptor descriptor) throws IOException;

    Iterable<URI> getSource(GameMap map) throws IOException;

    /**
     * Load the full map info contained in map.json of a map.
     * When listing available maps for a namespace, GameMap objects are obtained.
     * As a measure to reduce I/O-Operations, map.json files must be loaded separately.
     * Otherwise, the GameMap would only contain data directly from index.json.
     * @param descriptor The GameMap.
     * @return The {@link MapInfo} from map.json. This information will also be written to the GameMap object.
     */
    MapInfo loadMapInfo(MapDescriptor descriptor) throws IOException;
}
