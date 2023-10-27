package work.lclpnet.lobby.game.map;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class UriMapRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void testSimplePath() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);
        var maps = repo.getMapList("test");

        assertEquals(Set.of("map_one", "map_two", "map_three"), maps.stream()
                .map(MapRef::getPath)
                .collect(Collectors.toSet()));
    }

    @Test
    void testNestedPath() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);
        var maps = repo.getMapList("foo");

        assertEquals(Set.of("bar/baz", "bar/hi"), maps.stream()
                .map(MapRef::getPath)
                .collect(Collectors.toSet()));
    }

    @Test
    void testAbsolutePath() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);
        var maps = repo.getMapList("my_collection");

        assertEquals(Set.of("/test/map_two"), maps.stream()
                .map(MapRef::getPath)
                .collect(Collectors.toSet()));
    }

    @Test
    void testInfoSimple() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);

        var info = repo.getMapInfo("test/map_three");

        assertEquals(uri.resolve("test/map_three/map.json"), info.uri());
    }

    @Test
    void testInfoPropertiesMerged() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);

        var info = repo.getMapInfo("test/map_one");

        assertEquals(true, info.properties().get("extraProp"));
    }

    @Test
    void testInfoLink() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);

        var info = repo.getMapInfo("linked/test");

        assertEquals(uri.resolve("test/map_three/map.json"), info.uri());
    }

    @Test
    void testInfoLinkDataInherited() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);

        var info = repo.getMapInfo("linked/with_data");

        assertEquals(10, info.properties().get("inheritedProp"));
        assertEquals(true, info.properties().get("extraProp"));
    }

    @Test
    void testInfoLinkRelative() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);

        var info = repo.getMapInfo("linked/relative");

        assertEquals(uri.resolve("linked/relative/map/map.json"), info.uri());
    }

    @Test
    void testInfoLinkRelativeUp() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);

        var info = repo.getMapInfo("linked/relative/up");

        assertEquals(uri.resolve("linked/relative/map/map.json"), info.uri());
    }

    @Test
    void testInfoLinkMaxDepthExceededThrows() {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);

        assertThrows(IOException.class, () -> repo.getMapInfo("cycle/map_a"));
    }

    @Test
    void testInfoLinkTargetPropertyRemoved() throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);

        var info = repo.getMapInfo("linked/test");

        assertNull(info.properties().get("target"));
    }

    @Test
    void testInfoLinkOutsideOfRepoThrows() {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);

        assertThrows(IOException.class, () -> repo.getMapInfo("broken/escape"));
    }

    @Test
    void testGetMapsPathOutsideOfRepoThrows() {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repo = new UriMapRepository(uri, logger);

        assertThrows(IOException.class, () -> repo.getMapList("../escaped"));
    }
}
