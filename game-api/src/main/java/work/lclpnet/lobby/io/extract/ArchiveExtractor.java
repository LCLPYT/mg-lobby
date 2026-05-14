package work.lclpnet.lobby.io.extract;

import java.io.IOException;
import java.nio.file.Path;

public interface ArchiveExtractor {

    void extractTo(Path path) throws IOException;
}
