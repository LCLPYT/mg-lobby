package work.lclpnet.lobby.game.map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UriMapRepository implements MapRepository {

    private final URI root;
    private final Logger logger;

    public UriMapRepository(URI root, Logger logger) {
        this.root = root;
        this.logger = logger;
    }

    @Override
    public Collection<MapRef> getMapList(String path) throws IOException {
        URI indexUri = root.resolve(path + "/index.json");

        if (!indexUri.getPath().startsWith(root.getPath())) {
            throw new IOException("Path outside of repository");
        }

        JSONObject index = fetchJsonObject(indexUri);
        JSONArray mapsArray = index.getJSONArray("maps");

        Set<MapRef> maps = new HashSet<>();

        for (Object obj : mapsArray) {
            if (!(obj instanceof JSONObject json)) {
                logger.warn("Invalid json map array entry");
                continue;
            }

            Map<String, Object> props = new HashMap<>();

            for (String key : json.keySet()) {
                props.put(key, json.get(key));
            }

            maps.add(new MapRef(props));
        }

        return maps;
    }

    @Override
    public MapInfo getMapInfo(String path) throws IOException {
        return getMapInfo(root, path, 5);
    }

    private MapInfo getMapInfo(URI root, String path, final int maxLinkDepth) throws IOException {
        URI mapUri = root.resolve(path + "/map.json");

        if (!mapUri.getPath().startsWith(this.root.getPath())) {
            throw new IOException("Path outside of repository");
        }

        JSONObject json = fetchJsonObject(mapUri);

        Map<String, Object> props = new HashMap<>();

        for (String key : json.keySet()) {
            props.put(key, json.get(key));
        }

        Object targetObj = props.get("target");

        MapInfo info;

        if (targetObj instanceof String target) {
            if (maxLinkDepth <= 0) {
                throw new IOException("Too many links");
            }

            URI base;

            if (target.startsWith("/")) {
                base = this.root;
                target = target.substring(1);
            } else {
                base = mapUri;
            }

            info = getMapInfo(base, target, maxLinkDepth - 1);
            info.merge(props);
        } else {
            info = new MapInfo(mapUri, props);
        }

        return info;
    }

    private JSONObject fetchJsonObject(URI uri) throws IOException {
        String content;

        try (InputStream in = uri.toURL().openStream()) {
            content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        return new JSONObject(content);
    }
}
