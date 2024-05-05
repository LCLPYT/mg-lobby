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

        URI uri;

        try {
            uri = URI.create(source);
        } catch (Exception e) {
            // invalid URI
            return Optional.empty();
        }

        // check if source URI is a valid URL
        try {
            URL url = uri.toURL();

            // source is a valid URL, convert it back to URI
            return Optional.of(url.toURI());
        } catch (URISyntaxException ignored) {
            // invalid URI
            return Optional.empty();
        } catch (MalformedURLException | IllegalArgumentException ignored) {
            // source isn't a valid URL, use relative path behaviour instead...
        }

        var relativeUri = info.uri().resolve(source.replace('\\', '/'));

        return Optional.of(relativeUri);
    }
}
