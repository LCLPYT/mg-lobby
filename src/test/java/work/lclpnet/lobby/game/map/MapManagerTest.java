package work.lclpnet.lobby.game.map;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.lobby.game.asset.UriAssetRepository;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapManagerTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @ParameterizedTest
    @MethodSource("maps")
    void loadAll_loadedIntoCollection(String namespace, String path, Set<MapDescriptor> expected) throws IOException {
        URI uri = Path.of("src", "test", "resources", "maps").toUri();

        var repository = new AssetMapRepository(new UriAssetRepository(uri), logger);
        var lookup = new RepositoryMapLookup(repository);
        var fetcher = new DirectMapFetcher(lookup);
        var maps = new SimpleMapCollection();

        var manager = new MapManager(maps, lookup, fetcher);

        assertTrue(maps.getMaps().isEmpty());

        manager.loadAll(new MapDescriptor(namespace, path));

        var actual = maps.getMaps().stream()
                .map(GameMap::getDescriptor)
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> maps() {
        return Stream.of(
                Arguments.of("test", "", Set.of(
                        new MapDescriptor("test", "map_one"),
                        new MapDescriptor("test", "map_two"),
                        new MapDescriptor("test", "map_three")
                )),
                Arguments.of("my_collection", "", Set.of(
                        new MapDescriptor("test", "map_two")
                )),
                Arguments.of("linked", "", Set.of(
                        // target field not evaluated in list maps operation, only when trying to access the map
                        new MapDescriptor("linked", "test")
                )),
                Arguments.of("broken", "escape", Set.of(
                        // path is not validated in list maps operation, only when trying to access the map
                        new MapDescriptor("broken", "escape/../../../test")
                ))
        );
    }
}