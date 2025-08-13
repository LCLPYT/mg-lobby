package work.lclpnet.lobby.game.asset;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MultiAssetRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(MultiAssetRepositoryTest.class);

    @Test
    void returnsFromFirstSuccessfulRepository() throws IOException {
        AssetRepository repo1 = mock(AssetRepository.class);
        AssetRepository repo2 = mock(AssetRepository.class);

        AssetPath path = AssetPath.of("file.txt");

        when(repo1.get(path)).thenThrow(new IOException("not found"));
        when(repo2.get(path))
                .thenReturn(new AssetResult(new ByteArrayInputStream("ok".getBytes()), false));

        MultiAssetRepository multi = new MultiAssetRepository(new AssetRepository[]{repo1, repo2}, logger);

        try (var res = multi.get(path)) {
            assertEquals("ok", new String(res.resource().readAllBytes()));
        }

        verify(logger, atLeastOnce()).debug(anyString(), any(), any());
        verify(repo1).get(path);
        verify(repo2).get(path);
    }

    @Test
    void throwsWhenAllRepositoriesFail() throws IOException {
        AssetRepository repo1 = mock(AssetRepository.class);
        AssetRepository repo2 = mock(AssetRepository.class);
        Logger logger = mock(Logger.class);

        AssetPath path = AssetPath.of("missing");

        when(repo1.get(path)).thenThrow(new IOException("fail1"));
        when(repo2.get(path)).thenThrow(new IOException("fail2"));

        MultiAssetRepository multi = new MultiAssetRepository(new AssetRepository[]{repo1, repo2}, logger);

        IOException ex = assertThrows(IOException.class, () -> multi.get(path));
        assertEquals("Asset not found", ex.getMessage());

        verify(logger, atLeast(2)).debug(anyString(), any(), any());
    }

    @Test
    void continuesOnThrowableNotJustIOException() throws IOException {
        AssetRepository repo1 = mock(AssetRepository.class);
        AssetRepository repo2 = mock(AssetRepository.class);
        Logger logger = mock(Logger.class);

        AssetPath path = AssetPath.of("file.txt");

        when(repo1.get(path)).thenThrow(new RuntimeException("unexpected"));
        when(repo2.get(path))
                .thenReturn(new AssetResult(new ByteArrayInputStream("data".getBytes()), false));

        MultiAssetRepository multi = new MultiAssetRepository(new AssetRepository[]{repo1, repo2}, logger);

        try (var res = multi.get(path)) {
            assertEquals("data", new String(res.resource().readAllBytes()));
        }

        verify(logger, atLeastOnce()).debug(anyString(), any(), any());
    }
}