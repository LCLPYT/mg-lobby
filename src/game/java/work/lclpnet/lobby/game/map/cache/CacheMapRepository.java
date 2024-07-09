package work.lclpnet.lobby.game.map.cache;

import work.lclpnet.lobby.game.map.MapInfo;
import work.lclpnet.lobby.game.map.MapRef;
import work.lclpnet.lobby.game.map.MapRepository;

import java.io.IOException;
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
            return cached;
        }

        MapInfo mapInfo = upstream.getMapInfo(path);

        cache.cacheMapInfo(mapInfo.target(), mapInfo);

        return mapInfo;
    }

    @Override
    public Optional<URI> getResource(String path, String resource) {
        var cached = cache.getCachedResource(path, resource);

        if (cached != null) {
            return Optional.of(cached.toUri());
        }

        return upstream.getResource(path, resource).map(uri -> {
            Path cachedResource = cache.cacheResource(path, resource, uri);

            if (cachedResource == null) {
                return uri;
            }

            return cachedResource.toUri();
        });
    }
}
