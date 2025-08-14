package work.lclpnet.lobby.game.asset;

import java.io.IOException;

public interface AssetRepository {

    /**
     * Gets an asset directly as an {@link java.io.InputStream}, wrapped in a {@link AssetStreamResource}.
     * @param path The {@link AssetPath} towards the asset.
     * @param options Requests options for fetching the asset. Behaviour is handled entirely by the implementation.
     * @return An {@link AssetStreamResource} with the asset resource.
     * @throws IOException If the asset wasn't found or another I/O-error occurred.
     */
    AssetStreamResource getStream(AssetPath path, AssetRequestOptions options) throws IOException;

    /**
     * Gets {@link java.net.URI} of assets matching the given path.
     * Useful when needing to support local directories as assets, as directories may not really be opened as {@link java.io.InputStream}.
     * Provides an {@link java.util.Iterator} that computes the next {@link java.net.URI} on demand.
     * Callers are free not to consume all iterator items.
     * @param path The {@link AssetPath} towards the asset.
     * @param options Requests options for fetching the asset. Behaviour is handled entirely by the implementation.
     * @return An {@link java.util.Iterator} of {@link AssetUriResource}s for {@link java.net.URI}s matching the given {@link AssetPath}.
     */
    Iterable<AssetUriResource> getUris(AssetPath path, AssetRequestOptions options);

    /**
     * Gets an asset directly as an {@link java.io.InputStream}, wrapped in a {@link AssetStreamResource}.
     * @param path The {@link AssetPath} towards the asset.
     * @return An {@link AssetStreamResource} with the asset resource.
     * @throws IOException If the asset wasn't found or another I/O-error occurred.
     */
    default AssetStreamResource getStream(AssetPath path) throws IOException {
        return getStream(path, AssetRequestOptions.DEFAULT);
    }

    /**
     * Gets {@link java.net.URI} of assets matching the given path.
     * Useful when needing to support local directories as assets, as directories may not really be opened as {@link java.io.InputStream}.
     * Provides an {@link java.util.Iterator} that computes the next {@link java.net.URI} on demand.
     * Callers are free not to consume all iterator items.
     * @param path The {@link AssetPath} towards the asset.
     * @return An {@link java.util.Iterator} of {@link AssetUriResource}s for {@link java.net.URI}s matching the given {@link AssetPath}.
     */
    default Iterable<AssetUriResource> getUris(AssetPath path) {
        return getUris(path, AssetRequestOptions.DEFAULT);
    }
}
