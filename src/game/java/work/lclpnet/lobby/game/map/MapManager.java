package work.lclpnet.lobby.game.map;

import work.lclpnet.lobby.io.copy.WorldCopier;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class MapManager {

    private final MapCollection collection;
    private final MapLookup lookup;

    public MapManager(MapLookup lookup) {
        this(new SimpleMapCollection(), lookup);
    }

    public MapManager(MapCollection maps, MapLookup lookup) {
        this.collection = maps;
        this.lookup = lookup;
    }

    public MapCollection getCollection() {
        return collection;
    }

    /**
     * Pulls the world save of a {@link GameMap} into a directory.
     * @param map The map
     * @param target The target directory
     * @throws IOException If there was an IO error
     */
    public void pull(GameMap map, Path target) throws IOException {
        URI source = lookup.getSource(map).orElseThrow();

        WorldCopier.get(source).copyTo(target);
    }

    /**
     * Loads all maps from a given map path.<br>
     * Examples:
     * <code>loadAll(new MapDescriptor("hns", "", "1.20")</code>
     * will load all hide and seek maps with version 1.20.
     * <code>loadAll(new MapDescriptor("ap2", "spleef", "1.20"))</code>
     * will load all spleef maps for ArcadeParty2 with version 1.20.
     * @param descriptor The map descriptor; will load all children.
     */
    public void loadAll(MapDescriptor descriptor) throws IOException {
        var maps = lookup.getMaps(descriptor);
        collection.add(maps);
    }
}
