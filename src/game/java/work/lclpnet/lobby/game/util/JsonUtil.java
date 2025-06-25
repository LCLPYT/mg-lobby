package work.lclpnet.lobby.game.util;

import org.json.JSONObject;

public class JsonUtil {

    public static JSONObject copy(JSONObject src) {
        JSONObject dst = new JSONObject();

        putAll(src, dst);

        return dst;
    }

    public static void putAll(JSONObject src, JSONObject dst) {
        for (String key : src.keySet()) {
            Object val = src.get(key);
            dst.put(key, val);
        }
    }

    public static boolean equals(JSONObject x, JSONObject y) {
        return x == y || x != null && x.similar(y);
    }
}
