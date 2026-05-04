package work.lclpnet.lobby.game.util;

import org.json.JSONArray;
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

    public static JSONObject mergeJson(JSONObject template, JSONObject override) {
        JSONObject result = new JSONObject(template.toString());
        for (String rawKey : override.keySet()) {
            boolean replace = rawKey.endsWith("!");
            String key = replace ? rawKey.substring(0, rawKey.length() - 1) : rawKey;
            Object overrideVal = override.get(rawKey);
            if (!replace && result.has(key)) {
                Object existing = result.get(key);
                if (existing instanceof JSONObject ej && overrideVal instanceof JSONObject ov) {
                    result.put(key, mergeJson(ej, ov));
                    continue;
                }
                if (existing instanceof JSONArray ea && overrideVal instanceof JSONArray oa) {
                    JSONArray merged = new JSONArray();
                    ea.forEach(merged::put);
                    oa.forEach(merged::put);
                    result.put(key, merged);
                    continue;
                }
            }
            result.put(key, overrideVal);
        }
        return result;
    }
}
