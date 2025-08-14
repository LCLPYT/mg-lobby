package work.lclpnet.lobby.game.asset;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.lobby.game.asset.cache.AssetCache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheAssetRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(CacheAssetRepositoryTest.class);

    @Test
    void usesCacheIfAvailable() throws IOException {
        AssetCache cache = mock(AssetCache.class);
        AssetRepository upstream = mock(AssetRepository.class);
        CacheAssetRepository repo = new CacheAssetRepository(cache, upstream, 3600, logger);

        AssetPath path = AssetPath.of("cached.txt");
        Path tempFile = Files.createTempFile("cached", ".txt");
        Files.writeString(tempFile, "cachedData");

        when(cache.getCached(path)).thenReturn(Optional.of(tempFile));

        try (var res = repo.getStream(path)) {
            assertEquals("cachedData", new String(res.resource().readAllBytes()));
        }

        verify(upstream, never()).getStream(any(), any());
    }

    @Test
    void fetchesFromUpstreamIfNotCached() throws IOException {
        AssetCache cache = mock(AssetCache.class);
        AssetRepository upstream = mock(AssetRepository.class);
        CacheAssetRepository repo = new CacheAssetRepository(cache, upstream, 3600, logger);

        AssetPath path = AssetPath.of("new.txt");
        when(cache.getCached(path)).thenReturn(Optional.empty());
        when(upstream.getStream(eq(path), any()))
                .thenReturn(new AssetStreamResource(new ByteArrayInputStream("fresh".getBytes()), false));

        Path tempFile = Files.createTempFile("store", ".txt");
        Files.writeString(tempFile, "fresh");
        when(cache.cache(eq(path), (InputStream) any(), eq(3600))).thenReturn(tempFile);

        try (var res = repo.getStream(path)) {
            assertEquals("fresh", new String(res.resource().readAllBytes()));
        }
    }

    @Test
    void retriesUpstreamIfCachingFails() throws IOException {
        AssetCache cache = mock(AssetCache.class);
        AssetRepository upstream = mock(AssetRepository.class);
        CacheAssetRepository repo = new CacheAssetRepository(cache, upstream, 3600, logger);

        AssetPath path = AssetPath.of("retry.txt");
        when(cache.getCached(path)).thenReturn(Optional.empty());

        byte[] data = "data".getBytes();

        when(upstream.getStream(eq(path), any()))
                .thenReturn(new AssetStreamResource(new ByteArrayInputStream(data), false));

        when(cache.cache(eq(path), (InputStream) any(), eq(3600))).thenThrow(new IOException("fail"));

        try (var res = repo.getStream(path)) {
            assertEquals("data", new String(res.resource().readAllBytes()));
        }
    }

    @Test
    void returnsCachedUriIfPresent() {
        AssetCache cache = mock(AssetCache.class);
        AssetRepository upstream = mock(AssetRepository.class);
        Logger logger = mock(Logger.class);
        CacheAssetRepository repo = new CacheAssetRepository(cache, upstream, 3600, logger);

        AssetPath path = AssetPath.of("cached.txt");
        Path cachedPath = Path.of("/tmp/cached.txt");
        when(cache.getCached(path)).thenReturn(Optional.of(cachedPath));

        var uris = repo.getUris(path, AssetRequestOptions.DEFAULT);
        var it = uris.iterator();

        assertTrue(it.hasNext());
        assertEquals(cachedPath.toUri(), it.next().resource());
        assertFalse(it.hasNext());
        verify(upstream, never()).getUris(any(), any());
    }

    @Test
    void delegatesToUpstreamWhenNotCached() {
        AssetCache cache = mock(AssetCache.class);
        AssetRepository upstream = mock(AssetRepository.class);
        Logger logger = mock(Logger.class);
        CacheAssetRepository repo = new CacheAssetRepository(cache, upstream, 3600, logger);

        AssetPath path = AssetPath.of("remote.txt");
        when(cache.getCached(path)).thenReturn(Optional.empty());

        AssetUriResource upstreamResource = new AssetUriResource(URI.create("http://upstream/remote.txt"), false);
        when(upstream.getUris(path, AssetRequestOptions.DEFAULT))
                .thenReturn(List.of(upstreamResource));

        var uris = repo.getUris(path, AssetRequestOptions.DEFAULT);
        var it = uris.iterator();

        assertTrue(it.hasNext());
        assertEquals(upstreamResource, it.next());
        assertFalse(it.hasNext());
    }
}