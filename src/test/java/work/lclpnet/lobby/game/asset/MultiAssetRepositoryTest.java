package work.lclpnet.lobby.game.asset;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiAssetRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(MultiAssetRepositoryTest.class);

    @Test
    void returnsFromFirstSuccessfulRepository() throws IOException {
        AssetRepository repo1 = mock(AssetRepository.class);
        AssetRepository repo2 = mock(AssetRepository.class);

        AssetPath path = AssetPath.of("file.txt");

        when(repo1.getStream(eq(path), any()))
                .thenThrow(new IOException("not found"));

        when(repo2.getStream(eq(path), any()))
                .thenReturn(new AssetStreamResource(new ByteArrayInputStream("ok".getBytes()), false));

        MultiAssetRepository multi = new MultiAssetRepository(new AssetRepository[]{repo1, repo2}, logger);

        try (var res = multi.getStream(path)) {
            assertEquals("ok", new String(res.resource().readAllBytes()));
        }

        verify(repo1).getStream(eq(path), any());
        verify(repo2).getStream(eq(path), any());
    }

    @Test
    void throwsWhenAllRepositoriesFail() throws IOException {
        AssetRepository repo1 = mock(AssetRepository.class);
        AssetRepository repo2 = mock(AssetRepository.class);
        Logger logger = mock(Logger.class);

        AssetPath path = AssetPath.of("missing");

        when(repo1.getStream(eq(path), any()))
                .thenThrow(new IOException("fail1"));

        when(repo2.getStream(eq(path), any()))
                .thenThrow(new IOException("fail2"));

        MultiAssetRepository multi = new MultiAssetRepository(new AssetRepository[]{repo1, repo2}, logger);

        IOException ex = assertThrows(IOException.class, () -> multi.getStream(path));
        assertEquals("Asset not found: missing", ex.getMessage());

        verify(logger, atLeast(2)).debug(anyString(), any(), any());
    }

    @Test
    void continuesOnThrowableNotJustIOException() throws IOException {
        AssetRepository repo1 = mock(AssetRepository.class);
        AssetRepository repo2 = mock(AssetRepository.class);
        Logger logger = mock(Logger.class);

        AssetPath path = AssetPath.of("file.txt");

        when(repo1.getStream(eq(path), any()))
                .thenThrow(new RuntimeException("unexpected"));

        when(repo2.getStream(eq(path), any()))
                .thenReturn(new AssetStreamResource(new ByteArrayInputStream("data".getBytes()), false));

        MultiAssetRepository multi = new MultiAssetRepository(new AssetRepository[]{repo1, repo2}, logger);

        try (var res = multi.getStream(path)) {
            assertEquals("data", new String(res.resource().readAllBytes()));
        }

        verify(logger, atLeastOnce()).debug(anyString(), any(), any());
    }

    @Test
    void aggregatesUrisFromChildren() {
        AssetRepository child1 = mock(AssetRepository.class);
        AssetRepository child2 = mock(AssetRepository.class);
        Logger logger = mock(Logger.class);

        AssetPath path = AssetPath.of("asset.txt");

        when(child1.getUris(eq(path), eq(AssetRequestOptions.DEFAULT)))
                .thenReturn(List.of(new AssetUriResource(URI.create("http://repo1/asset.txt"), false)));
        when(child2.getUris(eq(path), eq(AssetRequestOptions.DEFAULT)))
                .thenReturn(List.of(new AssetUriResource(URI.create("http://repo2/asset.txt"), false)));

        MultiAssetRepository repo = new MultiAssetRepository(new AssetRepository[]{child1, child2}, logger);

        var uris = repo.getUris(path, AssetRequestOptions.DEFAULT);
        var it = uris.iterator();

        assertEquals("http://repo1/asset.txt", it.next().resource().toString());
        assertEquals("http://repo2/asset.txt", it.next().resource().toString());
        assertFalse(it.hasNext());
    }
}