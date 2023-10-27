package work.lclpnet.lobby.game.map;

import javax.annotation.Nullable;
import java.net.URI;
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
}
