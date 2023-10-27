package work.lclpnet.lobby.game.map;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapManagerTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void pull_copied() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);
        var manager = new MapManager(new RepositoryMapLookup(repo));

        manager.loadAll(new MapDescriptor("test", "", ""));

        var maps = manager.getCollection();

        Path dir = Files.createTempDirectory("mgl_mmt");

        for (String name : List.of("map_one", "map_two", "map_three")) {
            GameMap map = maps.getMap(new Identifier("test", name)).orElseThrow();

            Path path = dir.resolve("test").resolve(name);
            manager.pull(map, path);

            assertCopied(dir, path);
        }
    }

    @Test
    void pull_propertiesMerged() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);
        var manager = new MapManager(new RepositoryMapLookup(repo));

        manager.loadAll(new MapDescriptor("test", "", ""));

        var maps = manager.getCollection();

        Path dir = Files.createTempDirectory("mgl_mmt");

        GameMap map = maps.getMap(new Identifier("test", "map_one")).orElseThrow();

        assertNull(map.getProperty("extraProp"));

        manager.pull(map, dir.resolve("test").resolve("map_one"));

        assertEquals(Boolean.TRUE, map.getProperty("extraProp"));
    }

    @Test
    void pull_fromCollection_copied() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);
        var manager = new MapManager(new RepositoryMapLookup(repo));

        manager.loadAll(new MapDescriptor("my_collection", "", ""));

        var maps = manager.getCollection();

        Path dir = Files.createTempDirectory("mgl_mmt");

        GameMap map = maps.getMap(new Identifier("test", "map_two")).orElseThrow();

        Path path = dir.resolve("test").resolve("map_two");
        manager.pull(map, path);

        assertCopied(dir, path);
    }

    @Test
    void pull_linked_copied() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);
        var manager = new MapManager(new RepositoryMapLookup(repo));

        manager.loadAll(new MapDescriptor("linked", "", ""));

        var maps = manager.getCollection();

        Path dir = Files.createTempDirectory("mgl_mmt");

        GameMap map = maps.getMap(new Identifier("linked", "test")).orElseThrow();

        Path path = dir.resolve("test").resolve("map_three");
        manager.pull(map, path);

        assertCopied(dir, path);
    }

    private void assertCopied(Path dir, Path name) {
        assertEquals(Path.of("..", "..").toString(), name.relativize(dir).toString());

        Path path = dir.resolve(name).resolve("content.txt");
        assertTrue(Files.isRegularFile(path));
    }
}