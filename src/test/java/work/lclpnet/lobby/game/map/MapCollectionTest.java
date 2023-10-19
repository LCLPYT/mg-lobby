package work.lclpnet.lobby.game.map;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapCollectionTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void testGetMaps() throws IOException {
        var repo = getMapRepository();
        var manager = new MapCollection(repo, logger);

        manager.load("test");

        assertEquals(2, manager.getMaps().size());
    }

    @Test
    void testGetMap() throws IOException {
        MapRepository repo = getMapRepository();

        var manager = new MapCollection(repo, logger);

        manager.load("test");

        Stream.of("map_one", "nested/map_two")
                .map(id -> new Identifier("test", id))
                .forEach(id -> assertTrue(manager.getMap(id).isPresent()));
    }

    @Test
    void testMultiLoad() throws IOException {
        var repo = getMapRepository();
        var manager = new MapCollection(repo, logger);

        manager.load("test");
        manager.load("foo");

        assertEquals(Set.of("test:map_one", "test:nested/map_two", "foo:map_one", "foo:nested/map_two"),
                manager.getMaps().stream()
                        .map(map -> map.getIdentifier().toString())
                        .collect(Collectors.toSet()));
    }

    @NotNull
    private static MapRepository getMapRepository() throws IOException {
        MapRepository repo = mock();

        when(repo.getMaps(anyString())).thenAnswer(invocation -> {
            String namespace = invocation.getArgument(0);

            return Set.of(
                    new GameMap(new Identifier(namespace, "map_one"), Items.EMERALD),
                    new GameMap(new Identifier(namespace, "nested/map_two"), Items.RED_CANDLE, Map.of("author", "LCLP"))
            );
        });
        return repo;
    }
}
