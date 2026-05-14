package work.lclpnet.lobby.io;

import org.junit.jupiter.api.Test;
import work.lclpnet.lobby.io.copy.DirectoryWorldCopier;
import work.lclpnet.lobby.io.copy.UrlWorldCopier;
import work.lclpnet.lobby.io.copy.WorldCopier;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorldCopierTest {

    private static final Path TEST_RESOURCES = Path.of("src", "test", "resources");

    @Test
    void testGetterDirectory() {
        WorldCopier copier = WorldCopier.get(TEST_RESOURCES.resolve("directory").toUri());
        assertTrue(copier instanceof DirectoryWorldCopier);
    }

    @Test
    void testGetterUrl() {
        WorldCopier copier = WorldCopier.get(URI.create("https://lclpnet.work/dl/lobby-dl"));
        assertTrue(copier instanceof UrlWorldCopier);
    }

    @Test
    void testGetterLocalNotExisting() {
        assertThrows(IllegalStateException.class, () -> WorldCopier.get(URI.create("test/zip")));
    }

    @Test
    void testGetterLocalFile() {
        WorldCopier copier = WorldCopier.get(TEST_RESOURCES.resolve("directory.zip").toUri());
        assertTrue(copier instanceof UrlWorldCopier);
    }
}
