package work.lclpnet.game.map;

import org.json.JSONObject;
import org.slf4j.Logger;
import work.lclpnet.game.util.DependencySet;
import work.lclpnet.game.util.JsonUtil;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record MapRef(String path, JSONObject properties, DependencySet dependencies) {

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

    public static Optional<MapRef> create(JSONObject json, Logger logger) {
        String path = json.optString("path", null);

        if (path == null) {
            logger.error("String property \"path\" doesn't exist");
            return Optional.empty();
        }

        JSONObject depends = json.optJSONObject("depends", null);

        DependencySet dependencies = depends != null
                ? DependencySet.create(depends.toMap(), logger)
                : new DependencySet(Map.of());

        return Optional.of(new MapRef(path, json, dependencies));
    }
}
