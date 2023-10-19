package work.lclpnet.lobby.game.map;

import com.google.common.collect.ImmutableMap;
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
import java.util.Map;
import java.util.Optional;
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
        JSONObject index = getIndex(namespace);
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

    private JSONObject getIndex(String namespace) throws IOException {
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

        return new JSONObject(content);
    }

    @Override
    public Map<String, Object> getData(Identifier identifier) throws IOException {
        URI namespaceUri = getNamespaceUri(identifier);

        URL indexUrl = namespaceUri.resolve("index.json").toURL();
        String content;

        try (InputStream in = indexUrl.openStream()) {
            content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        JSONObject json = new JSONObject(content);

        var data = ImmutableMap.<String, Object>builder();

        for (String key : json.keySet()) {
            Object value = json.get(key);

            if (value == null) continue;

            data.put(key, value);
        }

        return data.build();
    }

    private URI getNamespaceUri(Identifier identifier) throws IOException {
        URI namespaceUri;

        try {
            namespaceUri = url.toURI()
                    .resolve(withSlash(identifier.getNamespace()))
                    .resolve(withSlash(identifier.getPath()));
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        return namespaceUri;
    }

    @Override
    public Optional<URI> getMapSource(Map<String, Object> data, Identifier identifier) throws IOException {
        String propName = "source";

        Object value = data.get(propName);

        if (!(value instanceof String source)) {
            logger.warn("String property \"{}\" doesn't exist for map {}", propName, identifier);
            return Optional.empty();
        }

        URI namespaceUri = getNamespaceUri(identifier);

        return Optional.of(namespaceUri.resolve(source));
    }

    private String withSlash(String s) {
        return s.endsWith("/") ? s : s.concat("/");
    }
}
