package work.lclpnet.lobby.game.map;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlMapRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void testSimplePath() throws IOException {
        URL url = Path.of("src", "test", "resources", "maps").toUri().toURL();

        var repo = new UrlMapRepository(url, logger);
        var maps = repo.getMaps("test");

        assertEquals(Set.of("map_one", "map_two", "map_three"), maps.stream()
                .map(map -> map.getIdentifier().getPath())
                .collect(Collectors.toSet()));
    }

    @Test
    void testNestedPath() throws IOException {
        URL url = Path.of("src", "test", "resources", "maps").toUri().toURL();

        var repo = new UrlMapRepository(url, logger);
        var maps = repo.getMaps("foo");

        assertEquals(Set.of("bar/baz"), maps.stream()
                .map(map -> map.getIdentifier().getPath())
                .collect(Collectors.toSet()));
    }
}
