package work.lclpnet.lobby.io.extract;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipExtractor implements ArchiveExtractor {

    private final Path zipPath;

    public ZipExtractor(Path zipPath) {
        this.zipPath = zipPath;
    }

    @Override
    public void extractTo(Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            Files.createDirectories(path);
        }

        try (var zip = new ZipFile(zipPath.toFile())) {
            var iterator = zip.entries().asIterator();

            while (iterator.hasNext()) {
                ZipEntry entry = iterator.next();

                Path dst = path.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(dst);
                } else {
                    Path parent = dst.getParent();

                    if (!Files.isDirectory(parent)) {
                        Files.createDirectories(parent);
                    }

                    try (var in = zip.getInputStream(entry)) {
                        Files.copy(in, dst);
                    }
                }
            }
        }
    }
}
