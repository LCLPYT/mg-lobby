package work.lclpnet.lobby.game.map;

import org.slf4j.Logger;
import work.lclpnet.lobby.io.copy.WorldCopier;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectMapFetcher implements MapFetcher {

    private final MapLookup lookup;
    private final Logger logger;

    public DirectMapFetcher(MapLookup lookup, Logger logger) {
        this.lookup = lookup;
        this.logger = logger;
    }

    @Override
    public void pull(GameMap map, Path target) throws IOException {
        for (URI uri : lookup.getSource(map)) {
            try {
                WorldCopier.get(uri).copyTo(target);
                return;
            } catch (IOException e) {
                logger.debug("Failed to copy world source of map {} to {}", map, target, e);
            }
        }

        throw new IOException("Failed to copy world source: No map could be copied successfully. Please check the debug log for more details");
    }
}
