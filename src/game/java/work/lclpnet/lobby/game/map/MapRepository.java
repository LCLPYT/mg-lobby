package work.lclpnet.lobby.game.map;

import work.lclpnet.lobby.game.asset.AssetPath;
import work.lclpnet.lobby.game.asset.AssetRequestOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;

public interface MapRepository {

    Collection<MapRef> getMapList(AssetPath path) throws IOException;

    MapInfo getMapInfo(AssetPath path) throws IOException;

    InputStream open(AssetPath path, AssetRequestOptions options) throws IOException;

    Iterable<URI> getUris(AssetPath path, AssetRequestOptions options);

    default InputStream open(AssetPath path) throws IOException {
        return open(path, AssetRequestOptions.DEFAULT);
    }

    default Iterable<URI> getUris(AssetPath path) {
        return getUris(path, AssetRequestOptions.DEFAULT);
    }
}
