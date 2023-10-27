package work.lclpnet.lobby.game.map;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RepositoryMapLookupTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void testGetMaps() throws IOException {
        var repo = getMapRepository();
        var lookup = new RepositoryMapLookup(repo);

        var maps = lookup.getMaps(new MapDescriptor("test", "", ""));

        assertEquals(List.of("test:map_one", "test:nested/map_two"), maps.stream()
                .map(map -> map.getDescriptor().getIdentifier())
                .map(Identifier::toString)
                .sorted()
                .toList());
    }

    @Test
    void testGetMapsNested() throws IOException {
        var repo = getMapRepository();
        var lookup = new RepositoryMapLookup(repo);

        var maps = lookup.getMaps(new MapDescriptor("test", "nested", ""));

        assertEquals(List.of("test:nested/map_two"), maps.stream()
                .map(map -> map.getDescriptor().getIdentifier())
                .map(Identifier::toString)
                .sorted()
                .toList());
    }

    @Test
    void testGetSourceDataLoaded() throws IOException {
        var repo = getMapRepository();
        var lookup = new RepositoryMapLookup(repo);

        GameMap map = new GameMap(new MapDescriptor("test", "hello", "1.20"), Items.DIAMOND);

        var source = lookup.getSource(map).orElseThrow();

        assertEquals(URI.create("test/hello/1.20/here"), source);
    }

    @NotNull
    private static MapRepository getMapRepository() {
        return new MapRepository() {
            @Override
            public Collection<MapRef> getMapList(String path) throws IOException {
                if ("test".equals(path)) {
                    return Set.of(
                            new MapRef(Map.of("path", "map_one")),
                            new MapRef(Map.of("path", "nested/map_two", "author", "LCLP"))
                    );
                }

                if ("test/nested".equals(path)) {
                    return Set.of(
                            new MapRef(Map.of("path", "map_two", "author", "LCLP"))
                    );
                }

                throw new IOException();
            }

            @Override
            public MapInfo getMapInfo(String path) throws IOException {
                if ("test/hello/1.20".equals(path)) {
                    return new MapInfo(URI.create("test/hello/1.20/map.json"), Map.of("source", "here"));
                }

                throw new IOException();
            }
        };
    }
}
