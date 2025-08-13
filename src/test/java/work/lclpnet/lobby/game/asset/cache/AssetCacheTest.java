package work.lclpnet.lobby.game.asset.cache;

import org.junit.jupiter.api.Test;
import work.lclpnet.lobby.game.asset.AssetPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetCacheTest {

    @Test
    void getCachedReturnsPathWhenValid() {
        CacheIndex index = mock(CacheIndex.class);
        Path root = Path.of("root");
        AssetCache cache = new AssetCache(index, root);
        AssetPath path = AssetPath.of("a", "b");

        when(index.isEntryInvalid(path.toString())).thenReturn(false);
        Optional<Path> result = cache.getCached(path);

        assertEquals(root.resolve(path.toPath()), result.orElseThrow());
    }

    @Test
    void getCachedReturnsEmptyWhenEmptyPathOrInvalid() {
        CacheIndex index = mock(CacheIndex.class);
        AssetCache cache = new AssetCache(index, Path.of("root"));
        assertTrue(cache.getCached(AssetPath.of()).isEmpty());

        AssetPath path = AssetPath.of("x");
        when(index.isEntryInvalid(path.toString())).thenReturn(true);
        assertTrue(cache.getCached(path).isEmpty());
    }

    @Test
    void cacheWritesFileCorrectly() throws IOException {
        CacheIndex index = mock(CacheIndex.class);
        Path tempDir = Files.createTempDirectory("mgl_act");
        AssetCache cache = new AssetCache(index, tempDir);

        AssetPath path = AssetPath.of("test", "file.txt");
        byte[] data = "testContent".getBytes();

        int ttlSeconds = 3600;

        try (InputStream in = new ByteArrayInputStream(data)) {
            Path stored = cache.cache(path, in, ttlSeconds);
            assertEquals(path.toPath(), tempDir.relativize(stored));
            assertTrue(Files.exists(stored));
            assertEquals("testContent", Files.readString(stored));
        }

        verify(index).updateEntry(eq("test/file.txt"), eq(ttlSeconds));
    }

    @Test
    void cacheEmptyPathThrows() {
        AssetCache cache = new AssetCache(mock(CacheIndex.class), Path.of("root"));
        assertThrows(IllegalArgumentException.class, () -> cache.cache(AssetPath.of(), InputStream.nullInputStream(), 3600));
    }

    @Test
    void invalidateInvoked() {
        CacheIndex index = mock(CacheIndex.class);
        AssetCache cache = new AssetCache(index, Path.of("root"));

        cache.invalidate(AssetPath.of("test", "foo"));

        verify(index, times(1)).invalidate("test/foo");
    }
}