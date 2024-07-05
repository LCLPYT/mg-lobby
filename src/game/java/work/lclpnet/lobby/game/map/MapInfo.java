package work.lclpnet.lobby.game.map;

import org.json.JSONObject;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public record MapInfo(URI uri, Map<String, Object> properties) {

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

        return new MapInfo(uri, copy);
    }
}
