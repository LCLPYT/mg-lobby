package work.lclpnet.lobby.io.extract;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class TarExtractor implements ArchiveExtractor {

    private final InputStream source;

    public TarExtractor(InputStream source) {
        this.source = source;
    }

    @Override
    public void extractTo(Path path) throws IOException {
        try (var in = new TarArchiveInputStream(source)) {
            TarArchiveEntry entry;
            while ((entry = in.getNextTarEntry()) != null) {
                Path dst = path.resolve(entry.getName());

                if (entry.isDirectory()) {
                    if (!Files.exists(dst)) {
                        Files.createDirectories(dst);
                    }
                } else {
                    Path parent = dst.getParent();
                    if (!Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }

                    Files.copy(in, dst);
                }
            }
        }
    }
}
