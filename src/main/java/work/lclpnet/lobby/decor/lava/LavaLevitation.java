package work.lclpnet.lobby.decor.lava;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public record LavaLevitation(List<Bounds> bounds, int durationTicks) {

    public JSONObject asJson() {
        JSONObject json = new JSONObject();
        JSONArray boundsJson = new JSONArray();

        for (Bounds bound : bounds) {
            boundsJson.put(bound.asJson());
        }

        json.put("bounds", boundsJson);

        json.put("duration_ticks", durationTicks);

        return json;
    }

    public boolean isWithinBounds(double x, double y, double z) {
        return bounds.stream().anyMatch(bound -> bound.contains(x, y, z));
    }

    public static LavaLevitation parse(JSONObject json) {
        JSONArray rawBounds = json.getJSONArray("bounds");

        List<Bounds> bounds = new ArrayList<>();

        for (Object entry : rawBounds) {
            if (!(entry instanceof JSONArray tuple)) continue;

            Bounds bound = Bounds.parse(tuple);
            bounds.add(bound);
        }

        int durationTicks = json.getInt("duration_ticks");

        return new LavaLevitation(bounds, durationTicks);
    }
}
