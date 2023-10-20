package work.lclpnet.lobby.io.copy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class DirectoryWorldCopier implements WorldCopier {

    private final Path source;

    public DirectoryWorldCopier(Path source) {
        this.source = Objects.requireNonNull(source);
    }

    @Override
    public void copyTo(Path path) throws IOException {
        if (!Files.isDirectory(source)) {
            throw new IOException("Source is not a directory");
        }

        copyRecursively(source, path);
    }

    private void copyRecursively(Path src, Path dst) throws IOException {
        if (Files.isRegularFile(src)) {
            Files.copy(src, dst);
            return;
        }

        if (!Files.exists(dst)) {
            Files.createDirectories(dst);
        }

        List<Path> children;
        try (var files = Files.list(src)) {
            children = files.toList();
        }

        for (Path child : children) {
            copyRecursively(child, dst.resolve(child.getFileName()));
        }
    }
}
