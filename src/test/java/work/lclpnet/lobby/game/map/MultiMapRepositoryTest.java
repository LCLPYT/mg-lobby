package work.lclpnet.lobby.game.map;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

        MapInfo mapA = new MapInfo(URI.create("foo/bar/1.20"), "foo/bar/1.20", Map.of("source", "sourceA"));
        MapInfo mapB = new MapInfo(URI.create("foo/bar/1.20"), "foo/bar/1.20", Map.of("source", "sourceB"));

        when(repoA.getMapInfo("foo/bar/1.20"))
                .thenReturn(mapA);

        when(repoB.getMapInfo("foo/bar/1.20"))
                .thenReturn(mapB);

        var multi = new MultiMapRepository(new MapRepository[] { repoA, repoB });

        MapInfo mapInfo = multi.getMapInfo("foo/bar/1.20");

        assertEquals(mapA, mapInfo);
    }

    @Test
    void getMapInfo_originUpdated() throws IOException {
        MapRepository repo = mock();

        MapInfo mapA = new MapInfo(URI.create("foo/bar/1.20"), "foo/bar/1.20", Map.of("source", "sourceA"));

        when(repo.getMapInfo("foo/bar/1.20"))
                .thenReturn(mapA);

        var multi = new MultiMapRepository(new MapRepository[] { repo });

        MapInfo mapInfo = multi.getMapInfo("foo/bar/1.20");

        assertNull(mapA.origin());
        assertSame(repo, mapInfo.origin());
    }

    @Test
    void getResource() {
        MapRepository repoA = mock();
        MapRepository repoB = mock();

        when(repoA.getResource(anyString(), any()))
                .thenReturn(Optional.empty());

        when(repoB.getResource(anyString(), any()))
                .thenReturn(Optional.empty());

        URI resOne = URI.create("foo/one/res1.txt");
        URI resTwo = URI.create("foo/two/bar/res2.txt");
        URI resThreeA = URI.create("foo/res.txt");
        URI resThreeB = URI.create("foo/res.txt");

        assertNotSame(resThreeA, resThreeB);

        when(repoA.getResource("foo/one", "res1.txt"))
                .thenReturn(Optional.of(resOne));

        when(repoB.getResource("foo/two", "bar/res2.txt"))
                .thenReturn(Optional.of(resTwo));

        when(repoA.getResource("foo/three", "../res.txt"))
                .thenReturn(Optional.of(resThreeA));

        when(repoB.getResource("foo/three", "../res.txt"))
                .thenReturn(Optional.of(resThreeB));

        var multi = new MultiMapRepository(new MapRepository[] { repoA, repoB });

        assertSame(resOne, multi.getResource("foo/one", "res1.txt").orElseThrow());
        assertSame(resTwo, multi.getResource("foo/two", "bar/res2.txt").orElseThrow());
        assertSame(resThreeA, multi.getResource("foo/three", "../res.txt").orElseThrow());
    }

    @Test
    void getResource_withInfo_originPreferred() {
        MapRepository repoA = mock();
        MapRepository repoB = mock();

        // repo A should not be invoked in this test case
        when(repoA.getResource(anyString(), any()))
                .thenThrow(AssertionError.class);

        when(repoB.getResource(anyString(), any()))
                .thenReturn(Optional.empty());

        URI resB = URI.create("foo/res.txt");

        when(repoB.getResource("foo/", "res.txt"))
                .thenReturn(Optional.of(resB));

        var multi = new MultiMapRepository(new MapRepository[] { repoA, repoB });

        // simulate info from repoB
        var info = new MapInfo(URI.create("some/uri"), "foo", new JSONObject(), repoB);

        // resource should be taken from repoB first, as the info originates from repoB
        assertSame(resB, multi.getResource(info, "res.txt").orElseThrow());
    }
}