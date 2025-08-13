package work.lclpnet.lobby.game.asset;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.lobby.game.asset.cache.AssetCache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

        try (InputStream in = repo.open(path)) {
            assertEquals("cachedData", new String(in.readAllBytes()));
        }

        verify(upstream, never()).open(any());
    }

    @Test
    void fetchesFromUpstreamIfNotCached() throws IOException {
        AssetCache cache = mock(AssetCache.class);
        AssetRepository upstream = mock(AssetRepository.class);
        CacheAssetRepository repo = new CacheAssetRepository(cache, upstream, 3600, logger);

        AssetPath path = AssetPath.of("new.txt");
        when(cache.getCached(path)).thenReturn(Optional.empty());
        when(upstream.open(path)).thenReturn(new ByteArrayInputStream("fresh".getBytes()));

        Path tempFile = Files.createTempFile("store", ".txt");
        Files.writeString(tempFile, "fresh");
        when(cache.cache(eq(path), any(), eq(3600))).thenReturn(tempFile);

        try (InputStream in = repo.open(path)) {
            assertEquals("fresh", new String(in.readAllBytes()));
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
        when(upstream.open(path)).thenReturn(new ByteArrayInputStream(data));
        when(cache.cache(eq(path), any(), eq(3600))).thenThrow(new IOException("fail"));

        try (InputStream in = repo.open(path)) {
            assertEquals("data", new String(in.readAllBytes()));
        }
    }
}