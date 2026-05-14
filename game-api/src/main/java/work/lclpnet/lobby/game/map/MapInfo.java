package work.lclpnet.lobby.game.map;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import work.lclpnet.lobby.game.util.JsonUtil;

import java.util.Map;
import java.util.Objects;

public record MapInfo(String target, JSONObject properties, @Nullable MapRepository origin) {

    public MapInfo(String target, Map<String, Object> properties) {
        this(target, new JSONObject(properties));
    }

    public MapInfo(String target, JSONObject properties) {
        this(target, properties, null);
    }

    @Nullable
    public String getSource() {
        return properties.optString("source", null);
    }

    public void merge(JSONObject props) {
        for (String key : props.keySet()) {
            if (properties.has(key) || "target".equals(key)) continue;

            Object val = props.get(key);

            properties.put(key, val);
        }
    }

    public void toJson(JSONObject json) {
        JsonUtil.putAll(properties, json);
    }

    public MapInfo withSource(String source) {
        JSONObject copy = JsonUtil.copy(properties);

        copy.put("source", source);

        return new MapInfo(target, copy, origin);
    }

    public MapInfo withOrigin(MapRepository repo) {
        return new MapInfo(target, properties, repo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapInfo mapInfo = (MapInfo) o;
        return Objects.equals(target, mapInfo.target) && JsonUtil.equals(properties, mapInfo.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, properties);
    }
}
