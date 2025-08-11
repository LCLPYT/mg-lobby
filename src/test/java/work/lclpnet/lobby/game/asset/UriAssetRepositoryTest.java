package work.lclpnet.lobby.game.asset;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UriAssetRepositoryTest {

    @Test
    void uriBuildsCorrectly() {
        URI root = URI.create("https://example.com/assets/");
        UriAssetRepository repo = new UriAssetRepository(root);
        AssetPath path = AssetPath.of("folder", "file name.txt");

        URI expected = root.resolve("folder/file%20name.txt");
        assertEquals(expected, repo.uri(path));
    }

    @Test
    void openReadsFromUrl() throws IOException {
        Path dir = Files.createTempDirectory("mgl_uar");
        Path path = dir.resolve("dir").resolve("hello.txt");

        Files.createDirectories(path.getParent());
        Files.writeString(path, "hello", UTF_8);

        UriAssetRepository repo = new UriAssetRepository(dir.toUri());

        try (InputStream in = repo.open(AssetPath.of("dir", "hello.txt"))) {
            assertEquals("hello", new String(in.readAllBytes(), UTF_8));
        }
    }

    @Test
    void toStringContainsRoot() {
        URI root = URI.create("https://example.com/");
        UriAssetRepository repo = new UriAssetRepository(root);
        assertTrue(repo.toString().contains("https://example.com/"));
    }
}