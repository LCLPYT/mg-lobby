package work.lclpnet.lobby.game.util;

import java.net.*;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FileUtil {

    private FileUtil() {}

    public static Optional<URI> getUri(URI base, String source) {
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

        var relativeUri = base.resolve(source.replace('\\', '/'));

        return Optional.of(relativeUri);
    }

    public static String encodeURIComponent(String s) {
        return URLEncoder.encode(s, UTF_8).replaceAll("\\+", "%20");
    }
}
