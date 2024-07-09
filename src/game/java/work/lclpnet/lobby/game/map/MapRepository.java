package work.lclpnet.lobby.game.map;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;

public interface MapRepository {

    Collection<MapRef> getMapList(String path) throws IOException;

    MapInfo getMapInfo(String path) throws IOException;

    Optional<URI> getResource(String path, String resource);

    default void addRedirectAction(MapRedirectAction action) {
        throw new UnsupportedOperationException("MapRepository does not support redirect actions");
    }
}
