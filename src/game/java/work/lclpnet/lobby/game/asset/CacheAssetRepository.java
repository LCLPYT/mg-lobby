package work.lclpnet.lobby.game.asset;

import com.google.common.collect.Iterators;
import org.slf4j.Logger;
import work.lclpnet.lobby.game.asset.cache.AssetCache;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

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
    public AssetStreamResource getStream(AssetPath path, AssetRequestOptions options) throws IOException {
        if (options.disableCache()) {
            logger.debug("Cache is disable for resource '{}', fetching from upstream {} ...", path, upstream);
            return upstream.getStream(path, options);
        }

        Path cachedPath = cache.getCached(path).orElse(null);

        if (cachedPath != null) {
            logger.debug("Using cached asset from '{}'", cachedPath);
            return new AssetStreamResource(Files.newInputStream(cachedPath), true);
        }

        logger.debug("Cache miss for asset '{}', fetching from upstream {} ...", path, upstream);

        try (var in = upstream.getStream(path, options)) {
            try {
                cachedPath = cache.cache(path, in.resource(), ttlSeconds);
            } catch (IOException e) {
                logger.error("Failed to cache asset '{}', refetching uncached...", path, e);
            }
        }

        if (cachedPath == null) {
            return upstream.getStream(path, options);
        }

        logger.debug("Asset '{}' has been cached to {}", path, cachedPath);

        return new AssetStreamResource(Files.newInputStream(cachedPath), false);
    }

    /**
     * Fetches {@link URI}s to a cached asset.
     * If no valid cached asset exists, the upstream is queried for the first existing asset, which is then cached.
     * If caching didn't work for any existing upstream asset, the upstream result is returned.
     * @param path The {@link AssetPath} towards the asset.
     * @param options The {@link AssetRequestOptions} for the request.
     * @return An {@link Iterable} of matching asset {@link URI}s.
     */
    @Override
    public Iterable<AssetUriResource> getUris(AssetPath path, AssetRequestOptions options) {
        if (options.disableCache()) {
            logger.debug("Cache is disable for resource '{}', fetching asset uris from upstream {} ...", path, upstream);
            return upstream.getUris(path, options);
        }

        Path cachedPath = cache.getCached(path).orElse(null);

        if (cachedPath != null) {
            logger.debug("Using uri for cached asset from '{}'", cachedPath);
            return wrap(cachedPath, true);
        }

        logger.debug("Cache miss for asset uri '{}', fetching from upstream {} ...", path, upstream);


        for (AssetUriResource res : upstream.getUris(path, options)) {
            try {
                cachedPath = cache.cache(path, res.resource(), ttlSeconds).orElse(null);
                break;
            } catch (IOException e) {
                logger.error("Failed to cache asset '{}', using uncached...", path, e);
            }
        }

        if (cachedPath == null) {
            return upstream.getUris(path, options);
        }

        logger.debug("Asset '{}' has been cached to {} from uri request", path, cachedPath);

        return wrap(cachedPath, false);
    }

    private Iterable<AssetUriResource> wrap(Path path, boolean cached) {
        URI uri;

        try {
            uri = path.toUri();
        } catch (Throwable t) {
            logger.error("Failed to convert path '{}' to uri", path, t);
            return Collections::emptyIterator;
        }

        var res = new AssetUriResource(uri, cached);

        return () -> Iterators.singletonIterator(res);
    }
}
