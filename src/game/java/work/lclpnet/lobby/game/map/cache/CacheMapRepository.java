package work.lclpnet.lobby.game.map.cache;

import work.lclpnet.lobby.game.map.MapInfo;
import work.lclpnet.lobby.game.map.MapRef;
import work.lclpnet.lobby.game.map.MapRepository;

import java.io.IOException;
import java.util.Collection;

/**
 * A map repository using cached maps first.
 */
public class CacheMapRepository implements MapRepository {

    private final MapRepository upstream;
    private final MapCache cache;

    public CacheMapRepository(MapRepository upstream, MapCache cache) {
        this.upstream = upstream;
        this.cache = cache;
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
            return cached;
        }

        MapInfo mapInfo = upstream.getMapInfo(path);

        cache.cacheMapInfo(path, mapInfo);

        return mapInfo;
    }
}
