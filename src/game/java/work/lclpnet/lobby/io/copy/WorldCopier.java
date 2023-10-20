package work.lclpnet.lobby.io.copy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public interface WorldCopier {

    /**
     * Copies the world contents to a path.
     * @param path The path to put the world contents to.
     */
    void copyTo(Path path) throws IOException;

    static WorldCopier get(URI uri) {
        if (uri.getHost() != null) {
            // uri is url
            try {
                return new UrlWorldCopier(uri.toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid url", e);
            }
        }

        // uri is local path
        Path path = uri.getScheme() != null ? Path.of(uri) : Path.of(uri.getPath());
        if (Files.isDirectory(path)) {
            return new DirectoryWorldCopier(path);
        } else if (Files.isRegularFile(path)) {
            try {
                return new UrlWorldCopier(path.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        throw new IllegalStateException("File '%s' does not exist".formatted(path));
    }
}
