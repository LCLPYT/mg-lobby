package work.lclpnet.lobby.game.map;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
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
        URL url = Path.of("src", "test", "resources", "maps").toUri().toURL();

        var repo = new UrlMapRepository(url, logger);
        var manager = new MapManager(repo, logger);

        MapCollection mapCollection = manager.getMapCollection();
        mapCollection.load("test");

        Path dir = Files.createTempDirectory("mgl_mmt");

        for (String name : List.of("map_one", "map_two", "map_three")) {
            GameMap map = mapCollection.getMap(new Identifier("test", name)).orElseThrow();

            Path path = dir.resolve("test").resolve(name);
            manager.pull(map, path);

            assertCopied(dir, path);
        }
    }

    @Test
    void pull_propertiesMerged() throws IOException {
        URL url = Path.of("src", "test", "resources", "maps").toUri().toURL();

        var repo = new UrlMapRepository(url, logger);
        var manager = new MapManager(repo, logger);

        MapCollection mapCollection = manager.getMapCollection();
        mapCollection.load("test");

        Path dir = Files.createTempDirectory("mgl_mmt");

        GameMap map = mapCollection.getMap(new Identifier("test", "map_one")).orElseThrow();

        assertNull(map.getProperty("extraProp"));

        manager.pull(map, dir.resolve("test").resolve("map_one"));

        assertEquals(Boolean.TRUE, map.getProperty("extraProp"));
    }

    private void assertCopied(Path dir, Path name) {
        assertEquals(Path.of("..", "..").toString(), name.relativize(dir).toString());

        Path path = dir.resolve(name).resolve("content.txt");
        assertTrue(Files.isRegularFile(path));
    }
}