package work.lclpnet.lobby.game.map.cache;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.lobby.game.map.GameMap;
import work.lclpnet.lobby.game.map.MapDescriptor;
import work.lclpnet.lobby.game.map.RepositoryMapLookup;
import work.lclpnet.lobby.game.map.UriMapRepository;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CacheMapFetcherTest {

    private static final Logger logger = LoggerFactory.getLogger("test");
    private CacheMapFetcher fetcher;
    private MapCache cache;

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    // local files should not be cached
    @Nested
    class Local {

        @BeforeEach
        public void setUp() throws IOException {
            URI uri = Path.of("src", "test", "resources", "maps").toUri();

            UriMapRepository upstreamRepo = new UriMapRepository(uri, logger);
            RepositoryMapLookup lookup = new RepositoryMapLookup(upstreamRepo);

            URI cacheUri = Files.createTempDirectory("mgl_cmf").toUri();
            UriMapRepository cacheRepo = new UriMapRepository(cacheUri, logger);

            cache = new MapCache(ALWAYS_CACHED, cacheRepo, 600, logger);
            fetcher = new CacheMapFetcher(lookup, cache);
        }

        @Test
        void pull_plain_copiedWithoutCache() throws IOException {
            Path dir = Files.createTempDirectory("mgl_mmt");

            for (String name : List.of("map_one", "map_two", "map_three")) {
                GameMap map = new GameMap(new MapDescriptor("test", name, ""));

                Path path = dir.resolve("test").resolve(name);

                fetcher.pull(map, path);

                assertCopied(dir, path);

                assertNull(cache.getCachedMapSource(map));
            }
        }

        @Test
        void pull_plain_propertiesMerged() throws IOException {
            Path dir = Files.createTempDirectory("mgl_mmt");

            GameMap map = new GameMap(new MapDescriptor("test", "map_one", ""));

            assertNull(map.getProperty("extraProp"));

            fetcher.pull(map, dir.resolve("test").resolve("map_one"));

            assertEquals(Boolean.TRUE, map.getProperty("extraProp"));
        }

        @Test
        void pull_linked_copiedWithoutCache() throws IOException {
            Path dir = Files.createTempDirectory("mgl_mmt");

            GameMap map = new GameMap(new MapDescriptor("linked", "test", ""));

            Path path = dir.resolve("test").resolve("map_three");

            fetcher.pull(map, path);

            assertCopied(dir, path);

            assertNull(cache.getCachedMapSource(map));
        }

        @Test
        void pull_escaped_throws() throws IOException {
            Path dir = Files.createTempDirectory("mgl_mmt");

            GameMap map = new GameMap(new MapDescriptor("broken", "escape/../../../test", ""));

            Path path = dir.resolve("broken").resolve("map_three");

            assertThrows(IOException.class, () -> fetcher.pull(map, path));
        }
    }

    // emulate remote files through http file server
    @Nested
    class Remote {

        static HttpServer server;

        @BeforeAll
        static void setUpServer() {
            InetSocketAddress address = new InetSocketAddress("localhost", 8000);
            Path rootPath = Path.of("src", "test", "resources", "maps").toAbsolutePath();

            server = SimpleFileServer.createFileServer(address, rootPath, SimpleFileServer.OutputLevel.INFO);
            server.start();
        }

        @AfterAll
        static void tearDownServer() {
            server.stop(1);
        }

        @BeforeEach
        public void setUp() throws IOException {
            URI uri = URI.create("http://localhost:8000");

            UriMapRepository upstreamRepo = new UriMapRepository(uri, logger);
            RepositoryMapLookup lookup = new RepositoryMapLookup(upstreamRepo);

            URI cacheUri = Files.createTempDirectory("mgl_cmf").toUri();
            UriMapRepository cacheRepo = new UriMapRepository(cacheUri, logger);

            cache = new MapCache(ALWAYS_CACHED, cacheRepo, 600, logger);
            fetcher = new CacheMapFetcher(lookup, cache);
        }

        @Test
        void pull_plain_copiedAndCached() throws IOException {
            Path dir = Files.createTempDirectory("mgl_mmt");

            GameMap map = new GameMap(new MapDescriptor("test", "map_three", ""));

            Path path = dir.resolve("test").resolve("map_three");

            fetcher.pull(map, path);

            assertCopied(dir, path);

            assertNotNull(cache.getCachedMapSource(map));
        }

        @Test
        void pull_plain_propertiesMerged() throws IOException {
            Path dir = Files.createTempDirectory("mgl_mmt");

            GameMap map = new GameMap(new MapDescriptor("test", "map_two", ""));

            assertNull(map.getProperty("extraProp"));

            fetcher.pull(map, dir.resolve("test").resolve("map_two"));

            assertEquals(10, (Integer) map.getProperty("extraProp"));
        }

        @Test
        void pull_linked_targetCopiedAndCached() throws IOException {
            Path dir = Files.createTempDirectory("mgl_mmt");

            // linked:test targets test:map_three, so test:map_three should be cached
            GameMap map = new GameMap(new MapDescriptor("linked", "test", ""));

            Path path = dir.resolve("test").resolve("map_three");

            fetcher.pull(map, path);

            assertCopied(dir, path);

            assertNotNull(cache.getCachedMapSource(map));
        }

        @Test
        void pull_escaped_throws() throws IOException {
            Path dir = Files.createTempDirectory("mgl_mmt");

            GameMap map = new GameMap(new MapDescriptor("broken", "escape/../../../test", ""));

            Path path = dir.resolve("broken").resolve("map_three");

            assertThrows(IOException.class, () -> fetcher.pull(map, path));
        }
    }

    private void assertCopied(Path dir, Path name) {
        assertEquals(Path.of("..", "..").toString(), name.relativize(dir).toString());

        Path path = dir.resolve(name).resolve("content.txt");
        assertTrue(Files.isRegularFile(path));
    }

    private static final CacheIndex ALWAYS_CACHED = new CacheIndex() {
        @Override
        public boolean isEntryInvalid(String path, int ttlSeconds) {
            return false;
        }

        @Override
        public void close() {}
    };
}