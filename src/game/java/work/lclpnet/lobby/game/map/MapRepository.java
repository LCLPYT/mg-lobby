package work.lclpnet.lobby.game.map;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

        try {
            URL url = new URL(source);

            return Optional.of(url.toURI());
        } catch (URISyntaxException e) {
            return Optional.empty();
        } catch (MalformedURLException ignored) {}

        var uri = info.uri().resolve(source.replace('\\', '/'));

        return Optional.of(uri);
    }
}
