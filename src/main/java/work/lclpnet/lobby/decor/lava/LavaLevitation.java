package work.lclpnet.lobby.decor.lava;

import org.json.JSONArray;
import org.json.JSONObject;

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
}
