package work.lclpnet.lobby.game.map;

import net.minecraft.util.Identifier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class UrlMapRepository implements MapRepository {

    private final URL url;
    private final Logger logger;

    public UrlMapRepository(URL url, Logger logger) {
        this.url = url;
        this.logger = logger;
    }

    @Override
    public Set<GameMap> getMaps(String namespace) throws IOException {
        URI namespaceUri;

        try {
            String withSlash = namespace.endsWith("/") ? namespace : namespace.concat("/");
            namespaceUri = url.toURI().resolve(withSlash);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        URL indexUrl = namespaceUri.resolve("index.json").toURL();

        String content;

        try (InputStream in = indexUrl.openStream()) {
            content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        JSONObject index = new JSONObject(content);
        JSONArray mapsArray = index.getJSONArray("maps");

        Set<GameMap> maps = new HashSet<>();

        for (Object obj : mapsArray) {
            if (!(obj instanceof JSONObject json)) {
                logger.warn("Invalid json map array entry");
                continue;
            }

            GameMap map = GameMap.parse(json, namespace);

            maps.add(map);
        }

        return maps;
    }

    @Override
    public URI getMapSource(Identifier identifier) throws IOException {
        URI namespaceUri;

        try {
            namespaceUri = url.toURI()
                    .resolve(withSlash(identifier.getNamespace()))
                    .resolve(withSlash(identifier.getPath()));

        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        URL indexUrl = namespaceUri.resolve("index.json").toURL();
        String content;

        try (InputStream in = indexUrl.openStream()) {
            content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        JSONObject json = new JSONObject(content);
        String source = json.getString("source");

        return namespaceUri.resolve(source);
    }

    private String withSlash(String s) {
        return s.endsWith("/") ? s : s.concat("/");
    }
}
