package work.lclpnet.game.api.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

public interface DataPackSink {

    /**
     * Offers a data pack to the sink.
     * This is a blocking operation.
     * @param id    The data pack id, e.g. "my_data.zip"
     * @param input The data pack source
     */
    void offer(Path id, InputStream input) throws IOException;

    /**
     * Gets all the data packs offered to the sink.
     * @return A set of data pack ids.
     */
    Set<Path> getIds();
}
