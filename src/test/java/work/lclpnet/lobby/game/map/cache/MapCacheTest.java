package work.lclpnet.lobby.game.map.cache;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.lobby.game.map.MapInfo;
import work.lclpnet.lobby.game.map.MapRef;
import work.lclpnet.lobby.game.map.UriMapRepository;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapCacheTest {

    static final Logger logger = LoggerFactory.getLogger(MapCacheTest.class);
    private MapCache cache;
    private Path cacheRoot;

    @BeforeEach
    void setUp() throws IOException {
        cacheRoot = Files.createTempDirectory("mgl_mc");

        UriMapRepository cacheRepo = new UriMapRepository(cacheRoot.toUri(), logger);

        cache = new MapCache(AlwaysCachedIndex.INSTANCE, cacheRepo, 3600, logger);
    }

    @Test
    void getCachedMapList_uncached_null() {
        var maps = cache.getCachedMapList("test");

        assertNull(maps);
    }

    @Test
    void cacheMapList_uncached_isCached() {
        Set<MapRef> maps = Set.of(
                new MapRef(Map.of("path", "map_one")),
                new MapRef(Map.of("path", "map_two", "icon", "minecraft:diamond")),
                new MapRef(Map.of(
                        "path", "map_three",
                        "authors", List.of("LCLP", "b_o_p_s"),
                        "any_property", new JSONObject(Map.of("weight", 0.75))))
        );

        cache.cacheMapList("test", maps);

        var cached = cache.getCachedMapList("test");

        assertEquals(maps, cached);
    }

    @Test
    void getCachedMapInfo_uncached_null() {
        var maps = cache.getCachedMapInfo("test/map_one");

        assertNull(maps);
    }

    @Test
    void cachedMapInfo_uncached_isCached() {
        URI uri = cacheRoot.resolve("test").resolve("map_one").resolve("map.json").toUri();
        MapInfo info = new MapInfo(uri, "test/map_one", Map.of("foo", "bar", "baz", 11, "test", true));

        cache.cacheMapInfo("test/map_one", info);

        var cached = cache.getCachedMapInfo("test/map_one");

        assertEquals(info, cached);
    }
}