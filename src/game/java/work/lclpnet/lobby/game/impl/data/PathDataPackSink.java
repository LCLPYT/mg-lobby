package work.lclpnet.lobby.game.impl.data;

import work.lclpnet.lobby.game.api.data.DataPackSink;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class PathDataPackSink implements DataPackSink {

    private final Path directory;
    private final Set<Path> ids = new HashSet<>();

    public PathDataPackSink(Path directory) {
        this.directory = directory;
    }

    @Override
    public void offer(Path id, InputStream input) throws IOException {
        Path target = directory.resolve(id.getFileName());

        Files.copy(input, target);

        ids.add(target);
    }

    @Override
    public Set<Path> getIds() {
        return ids;
    }
}
