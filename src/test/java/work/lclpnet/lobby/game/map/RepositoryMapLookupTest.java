package work.lclpnet.lobby.game.map;

import com.google.common.collect.Iterators;
import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import work.lclpnet.gaco.asset.AssetPath;
import work.lclpnet.gaco.asset.AssetRequestOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepositoryMapLookupTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void testGetMaps() throws IOException {
        var repo = getMapRepository();
        var lookup = new RepositoryMapLookup(repo);

        var maps = lookup.getMaps(new MapDescriptor("test", ""));

        assertEquals(List.of("test:map_one", "test:nested/map_two"), maps.stream()
                .map(map -> map.getDescriptor().getIdentifier())
                .map(ResourceLocation::toString)
                .sorted()
                .toList());
    }

    @Test
    void testGetMapsNested() throws IOException {
        var repo = getMapRepository();
        var lookup = new RepositoryMapLookup(repo);

        var maps = lookup.getMaps(new MapDescriptor("test", "nested"));

        assertEquals(List.of("test:nested/map_two"), maps.stream()
                .map(map -> map.getDescriptor().getIdentifier())
                .map(ResourceLocation::toString)
                .sorted()
                .toList());
    }

    @Test
    void testGetSourceDataLoaded() throws IOException {
        var repo = getMapRepository();
        var lookup = new RepositoryMapLookup(repo);

        GameMap map = new GameMap(new MapDescriptor("test", "hello"));

        var source = lookup.getSource(map).iterator().next();

        assertEquals(URI.create("test/hello/here"), source);
    }

    @Test
    void testGetSourceUnknownCacheRequestsWithCacheDisabled() throws IOException {
        var repo = mock(MapRepository.class);
        var lookup = new RepositoryMapLookup(repo);

        GameMap map = new GameMap(new MapDescriptor("test", "hello"));

        when(repo.getMapInfo(any()))
                .thenReturn(new MapInfo("test/hello", Map.of(
                        "source", "here"
                )));

        when(repo.getUris(any(), any())).then(invocation -> {
            AssetPath path = invocation.getArgument(0);
            AssetRequestOptions options = invocation.getArgument(1);

            assertTrue(options.disableCacheRead(), "Expected to fetch with disableCache=true");

            return (Iterable<URI>) () -> Iterators.singletonIterator(URI.create(path.toString()));
        });

        var source = lookup.getSource(map).iterator().next();

        assertEquals(URI.create("test/hello/here"), source);
    }

    @Test
    void testGetSourceUncachedRequestsWithCacheDisabled() throws IOException {
        var repo = mock(MapRepository.class);
        var lookup = new RepositoryMapLookup(repo);

        GameMap map = new GameMap(new MapDescriptor("test", "hello"));

        when(repo.getMapInfo(any()))
                .thenReturn(new MapInfo("test/hello", Map.of(
                        "source", "here",
                        AssetMapRepository.CACHED_PROPERTY, false
                )));

        when(repo.getUris(any(), any())).then(invocation -> {
            AssetPath path = invocation.getArgument(0);
            AssetRequestOptions options = invocation.getArgument(1);

            assertTrue(options.disableCacheRead(), "Expected to fetch with disableCache=true");

            return (Iterable<URI>) () -> Iterators.singletonIterator(URI.create(path.toString()));
        });

        var source = lookup.getSource(map).iterator().next();

        assertEquals(URI.create("test/hello/here"), source);
    }

    @Test
    void testGetSourceCachedRequestsWithCacheEnabled() throws IOException {
        var repo = mock(MapRepository.class);
        var lookup = new RepositoryMapLookup(repo);

        GameMap map = new GameMap(new MapDescriptor("test", "hello"));

        when(repo.getMapInfo(any()))
                .thenReturn(new MapInfo("test/hello", Map.of(
                        "source", "here",
                        AssetMapRepository.CACHED_PROPERTY, true
                )));

        when(repo.getUris(any(), any())).then(invocation -> {
            AssetPath path = invocation.getArgument(0);
            AssetRequestOptions options = invocation.getArgument(1);

            assertFalse(options.disableCacheRead(), "Expected to fetch with disableCache=false");

            return (Iterable<URI>) () -> Iterators.singletonIterator(URI.create(path.toString()));
        });

        var source = lookup.getSource(map).iterator().next();

        assertEquals(URI.create("test/hello/here"), source);
    }

    @NotNull
    private static MapRepository getMapRepository() {
        return new MapRepository() {
            @Override
            public Collection<MapRef> getMapList(AssetPath path) throws IOException {
                if ("test".equals(path.toString())) {
                    return Set.of(
                            new MapRef(Map.of("path", "map_one")),
                            new MapRef(Map.of("path", "nested/map_two", "author", "LCLP"))
                    );
                }

                if ("test/nested".equals(path.toString())) {
                    return Set.of(
                            new MapRef(Map.of("path", "map_two", "author", "LCLP"))
                    );
                }

                throw new IOException();
            }

            @Override
            public MapInfo getMapInfo(AssetPath path) throws IOException {
                if ("test/hello".equals(path.toString())) {
                    return new MapInfo("test/hello", Map.of("source", "here"));
                }

                throw new IOException();
            }

            @Override
            public InputStream open(AssetPath path, AssetRequestOptions options) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterable<URI> getUris(AssetPath path, AssetRequestOptions options) {
                return () -> Iterators.singletonIterator(URI.create(path.toString()));
            }
        };
    }
}
