package work.lclpnet.lobby.game.asset;

import org.slf4j.Logger;
import work.lclpnet.lobby.game.asset.cache.AssetCache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CacheAssetRepository implements AssetRepository {

    private final AssetCache cache;
    private final AssetRepository upstream;
    private final int ttlSeconds;
    private final Logger logger;

    public CacheAssetRepository(AssetCache cache, AssetRepository upstream, int ttlSeconds, Logger logger) {
        this.cache = cache;
        this.upstream = upstream;
        this.ttlSeconds = ttlSeconds;
        this.logger = logger;
    }

    @Override
    public AssetResult get(AssetPath path, AssetRequestOptions options) throws IOException {
        if (options.disableCache()) {
            logger.debug("Cache is disable for resource '{}', fetching from upstream {} ...", path, upstream);
            return upstream.get(path, options);
        }

        Path cachedPath = cache.getCached(path).orElse(null);

        if (cachedPath != null) {
            logger.debug("Using cached asset from '{}'", cachedPath);
            return new AssetResult(Files.newInputStream(cachedPath), true);
        }

        logger.debug("Cache miss for asset '{}', fetching from upstream {} ...", path, upstream);

        try (var in = upstream.get(path, options)) {
            try {
                cachedPath = cache.cache(path, in.resource(), ttlSeconds);
            } catch (IOException e) {
                logger.error("Failed to cache asset '{}', refetching uncached...", path, e);
            }
        }

        if (cachedPath == null) {
            return upstream.get(path, options);
        }

        logger.debug("Asset '{}' has been cached to {}", path, cachedPath);

        return new AssetResult(Files.newInputStream(cachedPath), false);
    }
}
