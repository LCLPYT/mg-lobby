package work.lclpnet.lobby.game.asset;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class UriAssetRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(UriAssetRepositoryTest.class);

    @Test
    void uriBuildsCorrectly() throws IOException {
        URI root = URI.create("https://example.com/assets/");
        UriAssetRepository repo = new UriAssetRepository(root, logger);
        AssetPath path = AssetPath.of("folder", "file name.txt");

        URI expected = root.resolve("folder/file%20name.txt");
        assertEquals(expected, repo.uri(path));
    }

    @Test
    void getStreamReadsFromUrl() throws IOException {
        Path dir = Files.createTempDirectory("mgl_uar");
        Path path = dir.resolve("dir").resolve("hello.txt");

        Files.createDirectories(path.getParent());
        Files.writeString(path, "hello", UTF_8);

        UriAssetRepository repo = new UriAssetRepository(dir.toUri(), logger);

        try (var res = repo.getStream(AssetPath.of("dir", "hello.txt"))) {
            assertEquals("hello", new String(res.resource().readAllBytes(), UTF_8));
        }
    }

    @Test
    void returnsSingleUriResource() {
        URI root = URI.create("https://example.com/assets/");
        UriAssetRepository repo = new UriAssetRepository(root, logger);

        AssetPath path = AssetPath.of("folder", "file.txt");
        Iterable<AssetUriResource> uris = repo.getUris(path, AssetRequestOptions.DEFAULT);

        var it = uris.iterator();
        assertTrue(it.hasNext());

        AssetUriResource resource = it.next();
        assertEquals(root.resolve("folder/file.txt"), resource.resource());
        assertFalse(it.hasNext());
    }

    @Test
    void toStringContainsRoot() {
        URI root = URI.create("https://example.com/");
        UriAssetRepository repo = new UriAssetRepository(root, logger);
        assertTrue(repo.toString().contains("https://example.com/"));
    }
}