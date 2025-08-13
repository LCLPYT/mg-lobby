package work.lclpnet.lobby.game.asset;

import java.io.IOException;

public interface AssetRepository {

    AssetResult get(AssetPath path, AssetRequestOptions options) throws IOException;

    default AssetResult get(AssetPath path) throws IOException {
        return get(path, AssetRequestOptions.DEFAULT);
    }
}
