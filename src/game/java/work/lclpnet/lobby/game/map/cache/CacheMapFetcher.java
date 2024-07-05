package work.lclpnet.lobby.game.map.cache;

import work.lclpnet.lobby.game.map.GameMap;
import work.lclpnet.lobby.game.map.MapFetcher;
import work.lclpnet.lobby.game.map.MapLookup;
import work.lclpnet.lobby.io.copy.WorldCopier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

/**
 * A {@link MapFetcher} that caches remote maps on the local file system.
 */
public class CacheMapFetcher implements MapFetcher {

    private final MapLookup lookup;
    private final MapCache cache;

    public CacheMapFetcher(MapLookup lookup, MapCache cache) {
        this.lookup = lookup;
        this.cache = cache;
    }

    @Override
    public void pull(GameMap map, Path target) throws IOException {
        URI source = lookup.getSource(map).orElseThrow();

        // check if source is a URL
        if (source.getHost() == null) {
            pullLocalMap(target, source);
            return;
        }

        URL url = source.toURL();

        if ("file".equalsIgnoreCase(url.getProtocol())) {
            pullLocalMap(target, source);
            return;
        }

        pullRemoteMap(target, source, map);
    }

    private void pullLocalMap(Path target, URI source) throws IOException {
        WorldCopier.get(source).copyTo(target);
    }

    private void pullRemoteMap(Path target, URI source, GameMap map) throws IOException {
        String mapPath = map.getDescriptor().getMapPath();
        URL sourceUrl;

        try {
            sourceUrl = source.toURL();
        } catch (MalformedURLException e) {
            // source is not a URL, continue like normal
            WorldCopier.get(source).copyTo(target);
            return;
        }

        Path cachedSource = cache.cacheMapSource(mapPath, sourceUrl);

        if (cachedSource == null) {
            // copy to target directly
            WorldCopier.get(source).copyTo(target);
            return;
        }

        URI cachedSourceUri = cachedSource.toUri();

        WorldCopier.get(cachedSourceUri).copyTo(target);
    }
}
