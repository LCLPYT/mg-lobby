package work.lclpnet.game.map;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class MapManager {

    private final MapCollection collection;
    private final MapLookup lookup;
    private final MapFetcher fetcher;

    public MapManager(MapLookup lookup, Logger logger) {
        this(lookup, new DirectMapFetcher(lookup, logger));
    }

    public MapManager(MapLookup lookup, MapFetcher fetcher) {
        this(new SimpleMapCollection(), lookup, fetcher);
    }

    public MapManager(MapCollection maps, MapLookup lookup, MapFetcher fetcher) {
        this.collection = maps;
        this.lookup = lookup;
        this.fetcher = fetcher;
    }

    public MapCollection getCollection() {
        return collection;
    }

    public MapLookup getLookup() {
        return lookup;
    }

    public MapFetcher getFetcher() {
        return fetcher;
    }

    /**
     * Pulls the world save of a {@link GameMap} into a directory.
     * @param map The map
     * @param target The target directory
     * @throws IOException If there was an IO error
     */
    public void pull(GameMap map, Path target) throws IOException {
        fetcher.pull(map, target);
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
