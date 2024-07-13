package work.lclpnet.lobby.game.map;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record MapInfo(URI uri, String target, Map<String, Object> properties, @Nullable MapRepository origin) {

    public MapInfo(URI uri, String target, Map<String, Object> properties) {
        this(uri, target, properties, null);
    }

    @Nullable
    public String getSource() {
        Object target = properties.get("source");

        if (target instanceof String str) {
            return str;
        }

        return null;
    }

    public void merge(Map<String, Object> props) {
        props.forEach((key, val) -> {
            if (properties.containsKey(key) || "target".equals(key)) return;

            properties.put(key, val);
        });
    }

    public void toJson(JSONObject json) {
        properties.forEach(json::put);
    }

    public MapInfo withSource(String source) {
        var copy = new HashMap<>(properties);

        copy.put("source", source);

        return new MapInfo(uri, target, copy, origin);
    }

    public MapInfo withOrigin(MapRepository repo) {
        return new MapInfo(uri, target, properties, repo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapInfo mapInfo = (MapInfo) o;
        return Objects.equals(uri, mapInfo.uri) && Objects.equals(target, mapInfo.target) && Objects.equals(properties, mapInfo.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, target, properties);
    }
}
