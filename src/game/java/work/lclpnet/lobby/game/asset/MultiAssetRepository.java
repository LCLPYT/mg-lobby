package work.lclpnet.lobby.game.asset;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class MultiAssetRepository implements AssetRepository {

    private final AssetRepository[] children;
    private final Logger logger;

    public MultiAssetRepository(AssetRepository[] children, Logger logger) {
        this.children = children;
        this.logger = logger;
    }

    @Override
    public InputStream open(AssetPath path) throws IOException {
        for (AssetRepository child : children) {
            logger.debug("Requesting asset '{}' from repository {} ...", path, child);

            try {
                return child.open(path);
            } catch (Throwable t) {
                logger.debug("Repository {} didn't contain asset '{}'", child, path);
            }
        }

        throw new IOException("Asset not found");
    }
}
