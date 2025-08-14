package work.lclpnet.lobby.game.asset;

import java.io.IOException;

public interface AssetRepository {

    AssetStreamResource getStream(AssetPath path, AssetRequestOptions options) throws IOException;

    Iterable<AssetUriResource> getUris(AssetPath path, AssetRequestOptions options);

    default AssetStreamResource getStream(AssetPath path) throws IOException {
        return getStream(path, AssetRequestOptions.DEFAULT);
    }

    default Iterable<AssetUriResource> getUris(AssetPath path) {
        return getUris(path, AssetRequestOptions.DEFAULT);
    }
}
