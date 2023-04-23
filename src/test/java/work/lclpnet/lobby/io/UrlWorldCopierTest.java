package work.lclpnet.lobby.io;

import org.junit.jupiter.api.Test;
import work.lclpnet.lobby.io.copy.UrlWorldCopier;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static work.lclpnet.lobby.io.DirectoryWorldCopierTest.assertCopyCorrect;

class UrlWorldCopierTest {

    private static final Path TEST_RESOURCES = Path.of("src", "test", "resources");

    @Test
    void testHttpsZip() throws IOException {
        URL url = new URL("https://lclpnet.work/dl/lobby-dl");
        Path dst = Files.createTempDirectory("mgl_uwc");

        try (var files = Files.list(dst)) {
            assertTrue(files.findAny().isEmpty());
        }

        new UrlWorldCopier(url).copyTo(dst);

        List<Path> checks = List.of(
                dst.resolve("lobby"),
                dst.resolve("lobby").resolve("level.dat"),
                dst.resolve("lobby").resolve("region"),
                dst.resolve("lobby").resolve("region").resolve("r.0.0.mca")
                // and more
        );

        checks.forEach(path -> assertTrue(Files.exists(path)));

        try (var files = Files.walk(dst)) {
            assertEquals(20, files.count());
        }
    }

    @Test
    void testLocalZip() throws IOException {
        URL url = TEST_RESOURCES.resolve("directory.zip").toUri().toURL();
        Path dst = Files.createTempDirectory("mgl_uwc");

        assertCopyCorrect(new UrlWorldCopier(url), dst);
    }

    @Test
    void testLocalTar() throws IOException {
        URL url = TEST_RESOURCES.resolve("directory.tar").toUri().toURL();
        Path dst = Files.createTempDirectory("mgl_uwc");

        assertCopyCorrect(new UrlWorldCopier(url), dst);
    }

    @Test
    void testLocalTarGz() throws IOException {
        URL url = TEST_RESOURCES.resolve("directory.tar.gz").toUri().toURL();
        Path dst = Files.createTempDirectory("mgl_uwc");

        assertCopyCorrect(new UrlWorldCopier(url), dst);
    }
}