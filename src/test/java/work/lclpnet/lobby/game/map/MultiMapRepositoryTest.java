package work.lclpnet.lobby.game.map;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultiMapRepositoryTest {

    @Test
    void getMapList() throws IOException {
        MapRepository repoA = mock();
        MapRepository repoB = mock();

        MapRef refA = new MapRef(Map.of("path", "foo/bar/1.20", "id", "mapA"));
        MapRef refB = new MapRef(Map.of("path", "foo/baz/1.20.1", "id", "mapB"));
        MapRef refC = new MapRef(Map.of("path", "foo/bar/1.20", "id", "mapC"));

        when(repoA.getMapList("test"))
                .thenReturn(Set.of(refA));

        when(repoB.getMapList("test"))
                .thenReturn(Set.of(refB, refC));

        var multi = new MultiMapRepository(new MapRepository[] { repoA, repoB });

        var maps = multi.getMapList("test");

        assertEquals(2, maps.size());

        var iterator = maps.iterator();
        MapRef first = iterator.next();
        MapRef second = iterator.next();

        // verify refA and refB are returned. refC should not be returned, because it has the same path as refA
        assertTrue(refA == first || refA == second);
        assertFalse(refA == first && refA == second);
        assertTrue(refB == first || refB == second);
    }

    @Test
    void getMapInfo() throws IOException {
        MapRepository repoA = mock();
        MapRepository repoB = mock();

        MapInfo mapA = new MapInfo(URI.create("foo/bar/1.20"), Map.of("source", "sourceA"));
        MapInfo mapB = new MapInfo(URI.create("foo/bar/1.20"), Map.of("source", "sourceB"));

        when(repoA.getMapInfo("foo/bar/1.20"))
                .thenReturn(mapA);

        when(repoB.getMapInfo("foo/bar/1.20"))
                .thenReturn(mapB);

        var multi = new MultiMapRepository(new MapRepository[] { repoA, repoB });

        MapInfo mapInfo = multi.getMapInfo("foo/bar/1.20");

        assertSame(mapA, mapInfo);
    }
}