package work.lclpnet.lobby.game.map;

import java.util.Map;
import java.util.Objects;

public class MapRef {

    private final String path;
    private final Map<String, Object> properties;

    public MapRef(Map<String, Object> properties) {
        this.properties = properties;

        Object pathObj = properties.get("path");

        if (!(pathObj instanceof String str)) {
            throw new AssertionError("String property \"path\" doesn't exist");
        }

        this.path = str;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getProperties() {
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
}
