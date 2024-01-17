package work.lclpnet.lobby.decor.greet;

import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.json.JSONObject;
import work.lclpnet.lobby.config.ConfigUtil;

public record GreetingConfig(Vec3d pos, float scale, float rotationY, Text text) {

    public JSONObject asJson() {
        JSONObject json = new JSONObject();

        json.put("position", ConfigUtil.writeVec3d(pos));
        json.put("scale", scale);
        json.put("rotation_y", rotationY);
        json.put("text", Text.Serialization.toJsonString(text));

        return json;
    }

    public static GreetingConfig parse(JSONObject json) {
        Vec3d position = ConfigUtil.readVec3d(json.getJSONArray("position"));
        float scale = ConfigUtil.readFloat(json.getNumber("scale"));
        float rotationY = ConfigUtil.readAngle(json.getNumber("rotation_y"));
        Text text = Text.Serialization.fromJson(json.getString("text"));

        return new GreetingConfig(position, scale, rotationY, text);
    }
}
