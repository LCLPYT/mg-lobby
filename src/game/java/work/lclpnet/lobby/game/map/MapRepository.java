package work.lclpnet.lobby.game.map;

import work.lclpnet.lobby.game.asset.AssetRequestOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;

public interface MapRepository {

    Collection<MapRef> getMapList(String path) throws IOException;

    MapInfo getMapInfo(String path) throws IOException;

    InputStream open(String path, AssetRequestOptions options) throws IOException;

    Iterable<URI> getUris(String path, AssetRequestOptions options);

    default InputStream open(String path) throws IOException {
        return open(path, AssetRequestOptions.DEFAULT);
    }

    default Iterable<URI> getUris(String path) {
        return getUris(path, AssetRequestOptions.DEFAULT);
    }

    default void addRedirectAction(MapRedirectAction action) {
        throw new UnsupportedOperationException("MapRepository does not support redirect actions");
    }
}
