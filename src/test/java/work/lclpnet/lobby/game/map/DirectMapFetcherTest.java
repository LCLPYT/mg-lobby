package work.lclpnet.lobby.game.map;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.lobby.game.asset.UriAssetRepository;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DirectMapFetcherTest {

    private static final Logger logger = LoggerFactory.getLogger("test");
    private DirectMapFetcher fetcher;

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @BeforeEach
    public void setUp() {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repository = new AssetMapRepository(new UriAssetRepository(uri), logger);

        RepositoryMapLookup lookup = new RepositoryMapLookup(repository);

        fetcher = new DirectMapFetcher(lookup);
    }

    @Test
    void pull_copied() throws IOException {
        Path dir = Files.createTempDirectory("mgl_mmt");

        for (String name : List.of("map_one", "map_two", "map_three")) {
            GameMap map = new GameMap(new MapDescriptor("test", name));

            Path path = dir.resolve("test").resolve(name);

            fetcher.pull(map, path);

            assertCopied(dir, path);
        }
    }

    @Test
    void pull_propertiesMerged() throws IOException {
        Path dir = Files.createTempDirectory("mgl_mmt");

        GameMap map = new GameMap(new MapDescriptor("test", "map_one"));

        assertNull(map.getProperty("extraProp"));

        fetcher.pull(map, dir.resolve("test").resolve("map_one"));

        assertEquals(Boolean.TRUE, map.getProperty("extraProp"));
    }

    @Test
    void pull_linked_copied() throws IOException {
        Path dir = Files.createTempDirectory("mgl_mmt");

        GameMap map = new GameMap(new MapDescriptor("linked", "test"));

        Path path = dir.resolve("test").resolve("map_three");

        fetcher.pull(map, path);

        assertCopied(dir, path);
    }

    @Test
    void pull_escaped_throws() throws IOException {
        Path dir = Files.createTempDirectory("mgl_mmt");

        GameMap map = new GameMap(new MapDescriptor("broken", "escape/../../../test"));

        Path path = dir.resolve("broken").resolve("map_three");

        assertThrows(IOException.class, () -> fetcher.pull(map, path));
    }

    private void assertCopied(Path dir, Path name) {
        assertEquals(Path.of("..", "..").toString(), name.relativize(dir).toString());

        Path path = dir.resolve(name).resolve("content.txt");
        assertTrue(Files.isRegularFile(path));
    }
}