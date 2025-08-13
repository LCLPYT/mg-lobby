package work.lclpnet.lobby.game.map.cache;

import work.lclpnet.lobby.game.map.MapInfo;
import work.lclpnet.lobby.game.map.MapRef;
import work.lclpnet.lobby.game.map.MapRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

/**
 * A map repository using cached maps first.
 */
public class CacheMapRepository implements MapRepository {

    private final MapRepository upstream;
    private final MapCache cache;

    public CacheMapRepository(MapRepository upstream, MapCache cache) {
        this.upstream = upstream;
        this.cache = cache;
        this.upstream.addRedirectAction(this.cache::cacheMapInfo);
    }

    @Override
    public Collection<MapRef> getMapList(String path) throws IOException {
        var cached = cache.getCachedMapList(path);

        if (cached != null) {
            return cached;
        }

        Collection<MapRef> mapList = upstream.getMapList(path);

        cache.cacheMapList(path, mapList);

        return mapList;
    }

    @Override
    public MapInfo getMapInfo(String path) throws IOException {
        var cached = cache.getCachedMapInfo(path);

        if (cached != null) {
            return cached.withOrigin(this);
        }

        MapInfo mapInfo = upstream.getMapInfo(path);

        cache.cacheMapInfo(mapInfo.target(), mapInfo);

        // invalidate the map source to keep it in sync with the info
        cache.invalidateSource(mapInfo);  // TODO important!!

        return mapInfo.withOrigin(this);
    }

    @Override
    public InputStream open(String path) throws IOException {
        throw new IOException("unsupported");
    }
}
