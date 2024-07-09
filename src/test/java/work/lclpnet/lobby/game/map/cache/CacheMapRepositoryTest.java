package work.lclpnet.lobby.game.map.cache;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import org.apache.commons.io.function.IOFunction;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.lobby.game.map.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CacheMapRepositoryTest {

    static final Logger logger = LoggerFactory.getLogger(CacheMapRepositoryTest.class);

    static HttpServer server;
    static UriMapRepository _upstream;
    private MapCache cache;

    @BeforeAll
    static void setUpServer() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC", true, SqliteCacheIndex.class.getClassLoader());

        InetSocketAddress address = new InetSocketAddress("localhost", 8000);
        Path rootPath = Path.of("src", "test", "resources", "maps").toAbsolutePath();

        server = SimpleFileServer.createFileServer(address, rootPath, SimpleFileServer.OutputLevel.INFO);
        server.start();

        URI uri = URI.create("http://localhost:8000");

        _upstream = new UriMapRepository(uri, logger);
    }

    @AfterAll
    static void tearDownServer() {
        server.stop(1);
    }

    WrappedMapRepository upstream;
    CacheMapRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        Path root = Files.createTempDirectory("mgl_cmr");
        cache = MapCache.createCache(root, logger);
        upstream = new WrappedMapRepository(_upstream);
        repository = new CacheMapRepository(upstream, cache);
    }

    @ParameterizedTest
    @MethodSource("mapListArgs")
    void getMapList_notCached_cacheFromUpstream(String path, Set<String> mapsPaths) throws IOException {
        assertNull(cache.getCachedMapList(path));

        Collection<MapRef> maps = repository.getMapList(path);

        assertEquals(mapsPaths, paths(maps));

        Collection<MapRef> cached = cache.getCachedMapList(path);

        assertNotNull(cached);
        assertEquals(mapsPaths, paths(cached));
    }

    @ParameterizedTest
    @MethodSource("mapListArgs")
    void getMapList_cached_getFromCache(String path) throws IOException {
        // preload once
        var expected = paths(repository.getMapList(path));

        // upstream repository should not be called
        upstream.disableMapList();

        // map list should be fetched from cache
        var actual = paths(repository.getMapList(path));

        assertEquals(expected, actual);
    }

    static Stream<Arguments> mapListArgs() {
        return Stream.of(
                Arguments.of("test", Set.of("map_one", "map_two", "map_three")),
                Arguments.of("my_collection", Set.of("/test/map_two")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test/map_two", "test/map_three"})
    void getMapInfo_plain_notCached_cacheFromUpstream(String path) throws IOException {
        assertNull(cache.getCachedMapInfo(path));

        MapInfo info = repository.getMapInfo(path);

        assertEquals(path, info.target());  // plain

        MapInfo cached = cache.getCachedMapInfo(path);

        assertNotNull(cached);
        assertNotEquals(info.uri(), cached.uri());
        assertEquals(info.target(), cached.target());
        assertEquals(info.properties(), cached.properties());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test/map_two", "test/map_three"})
    void getMapInfo_plain_cached_getFromCache(String path) throws IOException {
        MapInfo expected = repository.getMapInfo(path);

        upstream.disableMapInfo();

        MapInfo actual = repository.getMapInfo(path);

        assertNotNull(actual);
        assertNotEquals(expected.uri(), actual.uri());
        assertEquals(expected.target(), actual.target());
        assertEquals(expected.properties(), actual.properties());
    }

    @ParameterizedTest
    @CsvSource({"linked/test,test/map_three", "linked/relative,linked/relative/map", "linked/relative/up,linked/relative/map"})
    void getMapInfo_linkedAbsolute_nonCached_cacheFromUpstream(String path, String target) throws IOException {
        assertNull(cache.getCachedMapInfo(path));

        MapInfo info = repository.getMapInfo(path);

        assertEquals(target, info.target());  // linked

        MapInfo cached = cache.getCachedMapInfo(path);

        assertNotNull(cached);
        assertNotEquals(info.uri(), cached.uri());
        assertEquals(info.target(), cached.target());
        assertEquals(info.properties(), cached.properties());

        // should be the same as path
        cached = cache.getCachedMapInfo(target);

        assertNotNull(cached);
        assertNotEquals(info.uri(), cached.uri());
        assertEquals(info.target(), cached.target());
        assertEquals(info.properties(), cached.properties());
    }

    @ParameterizedTest
    @ValueSource(strings = {"linked/test", "linked/relative", "linked/relative/up"})
    void getMapInfo_linkedAbsolute_cached_getFromCache(String path) throws IOException {
        MapInfo expected = repository.getMapInfo(path);

        upstream.disableMapInfo();

        MapInfo actual = repository.getMapInfo(path);

        assertNotEquals(expected.uri(), actual.uri());
        assertEquals(expected.target(), actual.target());
        assertEquals(expected.properties(), actual.properties());
    }

    @ParameterizedTest
    @CsvSource({"test/map_three,world.tar.xz", "test/map_two,world.zip"})
    void getResource_uncached_cacheFromUpstream(String path, String resource) throws IOException {
        assertNull(cache.getCachedResource(path, resource));

        repository.getResource(path, resource).orElseThrow();

        assertNotNull(cache.getCachedResource(path, resource));
    }

    @ParameterizedTest
    @CsvSource({"test/map_three,world.tar.xz", "test/map_two,world.zip"})
    void getResource_cached_getFromCache(String path, String resource) throws IOException {
        URI expected = repository.getResource(path, resource).orElseThrow();

        upstream.disableResources();

        URI actual = repository.getResource(path, resource).orElseThrow();

        assertEquals(expected, actual);
    }

    @Test
    void getMapInfo_infoUncachedSourceCached_sourceInvalidated() throws IOException {
        URI uri = repository.getResource("test/map_three", "world.tar.xz").orElseThrow();
        Path cached = cache.getCachedResource("test/map_three", "world.tar.xz");

        assertNotNull(cached);
        assertEquals(uri, cached.toUri());

        repository.getMapInfo("test/map_three");

        assertNull(cache.getCachedResource("test/map_three", "world.tar.xz"));
    }

    private static @NotNull Set<String> paths(Collection<MapRef> maps) {
        return maps.stream()
                .map(MapRef::getPath)
                .collect(Collectors.toSet());
    }

    private static class WrappedMapRepository implements MapRepository {

        final MapRepository parent;
        IOFunction<String, Collection<MapRef>> mapListFunction = null;
        IOFunction<String, MapInfo> mapInfoFunction = null;
        BiFunction<String, String, Optional<URI>> resourceFunction = null;

        private WrappedMapRepository(MapRepository parent) {
            this.parent = Objects.requireNonNull(parent);
        }

        @Override
        public Collection<MapRef> getMapList(String path) throws IOException {
            if (mapListFunction != null) {
                return mapListFunction.apply(path);
            }

            return parent.getMapList(path);
        }

        @Override
        public MapInfo getMapInfo(String path) throws IOException {
            if (mapInfoFunction != null) {
                return mapInfoFunction.apply(path);
            }

            return parent.getMapInfo(path);
        }

        @Override
        public Optional<URI> getResource(String path, String resource) {
            if (resourceFunction != null) {
                return resourceFunction.apply(path, resource);
            }

            return parent.getResource(path, resource);
        }

        @Override
        public void addRedirectAction(MapRedirectAction action) {
            parent.addRedirectAction(action);
        }

        public void disableMapList() {
            mapListFunction = path -> fail("Did not expect getMapList() to be called");
        }

        public void disableMapInfo() {
            mapListFunction = path -> fail("Did not expect getMapInfo() to be called");
        }

        public void disableResources() {
            resourceFunction = (path, res) -> fail("Did not expect getResource() to be called");
        }
    }
}