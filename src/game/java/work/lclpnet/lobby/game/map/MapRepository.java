package work.lclpnet.lobby.game.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface MapRepository {

    Collection<MapRef> getMapList(String path) throws IOException;

    MapInfo getMapInfo(String path) throws IOException;

    InputStream open(String path) throws IOException;

    default void addRedirectAction(MapRedirectAction action) {
        throw new UnsupportedOperationException("MapRepository does not support redirect actions");
    }
}
