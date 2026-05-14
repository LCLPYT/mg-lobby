package work.lclpnet.lobby.io;

import org.junit.jupiter.api.Test;
import work.lclpnet.lobby.io.copy.DirectoryWorldCopier;
import work.lclpnet.lobby.io.copy.WorldCopier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectoryWorldCopierTest {

    @Test
    void testCopyTo() throws IOException {
        Path src = Path.of("src", "test", "resources", "directory");
        Path dst = Files.createTempDirectory("mgl_dwc");

        assertCopyCorrect(new DirectoryWorldCopier(src), dst);
    }

    public static void assertCopyCorrect(WorldCopier copier, Path dst) throws IOException {
        try (var files = Files.list(dst)) {
            assertTrue(files.findAny().isEmpty());
        }

        copier.copyTo(dst);

        assertIsTestContent(dst);
    }

    public static void assertIsTestContent(Path dst) throws IOException {
        List<Path> checks = List.of(
                dst.resolve("bar.json"),
                dst.resolve("foo.txt"),
                dst.resolve("nested"),
                dst.resolve("nested").resolve("baz.yml"),
                dst.resolve("nested").resolve("further_nested"),
                dst.resolve("nested").resolve("further_nested").resolve("hello_world.md")
        );

        checks.forEach(path -> assertTrue(Files.exists(path)));

        try (var files = Files.walk(dst)) {
            assertEquals(7, files.count());
        }
    }
}