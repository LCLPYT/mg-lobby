package work.lclpnet.lobby.game.asset;

import com.google.common.collect.AbstractIterator;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Iterator;

public class MultiAssetRepository implements AssetRepository {

    private final AssetRepository[] children;
    private final Logger logger;

    public MultiAssetRepository(AssetRepository[] children, Logger logger) {
        this.children = children;
        this.logger = logger;
    }

    @Override
    public Iterable<AssetUriResource> getUris(AssetPath path, AssetRequestOptions options) {
        return () -> new AbstractIterator<>() {
            private int i = 0;
            private Iterator<AssetUriResource> current = null;

            @Override
            protected AssetUriResource computeNext() {
                boolean childHasNext;

                while ((childHasNext = current != null && current.hasNext()) || i < children.length) {
                    if (childHasNext) {
                        return current.next();
                    }

                    AssetRepository child = children[i++];
                    logger.debug("Requesting asset uris for '{}' from repository {} ...", path, child);
                    current = child.getUris(path, options).iterator();
                }

                endOfData();
                return null;
            }
        };
    }

    @Override
    public AssetStreamResource getStream(AssetPath path, AssetRequestOptions options) throws IOException {
        for (AssetRepository child : children) {
            logger.debug("Requesting asset '{}' from repository {} ...", path, child);

            try {
                return child.getStream(path, options);
            } catch (Throwable t) {
                logger.debug("Repository {} didn't contain asset '{}'", child, path);
            }
        }

        throw new IOException("Asset not found");
    }
}
