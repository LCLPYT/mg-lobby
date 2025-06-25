package work.lclpnet.lobby.game.map;

import org.json.JSONObject;
import work.lclpnet.lobby.game.util.JsonUtil;

import java.util.Map;
import java.util.Objects;

public class MapRef {

    private final String path;
    private final JSONObject properties;

    public MapRef(Map<String, Object> properties) {
        this(new JSONObject(properties));
    }

    public MapRef(JSONObject properties) {
        this.properties = properties;

        String str = properties.optString("path", null);

        if (str == null) {
            throw new AssertionError("String property \"path\" doesn't exist");
        }

        this.path = str;
    }

    public String getPath() {
        return path;
    }

    public JSONObject getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapRef mapRef = (MapRef) o;
        return Objects.equals(path, mapRef.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public void toJson(JSONObject json) {
        JsonUtil.putAll(properties, json);
    }
}
