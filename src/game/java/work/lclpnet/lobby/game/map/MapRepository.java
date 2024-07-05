package work.lclpnet.lobby.game.map;

import work.lclpnet.lobby.game.util.FileUtil;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;

public interface MapRepository {

    Collection<MapRef> getMapList(String path) throws IOException;

    MapInfo getMapInfo(String path) throws IOException;

    default Optional<URI> getMapSource(MapInfo info) {
        String source = info.getSource();

        if (source == null) {
            return Optional.empty();
        }

        return FileUtil.getUri(info.uri(), source);
    }
}
