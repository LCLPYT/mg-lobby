package work.lclpnet.lobby.decor.greet;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import work.lclpnet.lobby.config.ConfigUtil;

public record GreetingConfig(Vec3d pos, float scale, float rotationY, Text text) {

    public JSONObject asJson(RegistryWrapper.WrapperLookup registries) {
        JSONObject json = new JSONObject();

        json.put("position", ConfigUtil.writeVec3d(pos));
        json.put("scale", scale);
        json.put("rotation_y", rotationY);

        encodeText(registries, json);

        return json;
    }

    public static GreetingConfig parse(JSONObject json, RegistryWrapper.WrapperLookup registries) {
        Vec3d position = ConfigUtil.readVec3d(json.getJSONArray("position"));
        float scale = ConfigUtil.readFloat(json.getNumber("scale"));
        float rotationY = ConfigUtil.readAngle(json.getNumber("rotation_y"));

        Text text = decodeText(json, registries);

        return new GreetingConfig(position, scale, rotationY, text);
    }

    private void encodeText(RegistryWrapper.WrapperLookup registries, JSONObject json) {
        String textJson = TextCodecs.CODEC.encodeStart(registries.getOps(JsonOps.INSTANCE), text)
                .resultOrPartial()
                .map(JsonElement::toString)
                .orElse("");

        json.put("text", new JSONObject(textJson));
    }

    private static @NotNull Text decodeText(JSONObject json, RegistryWrapper.WrapperLookup registries) {
        String str = json.optString("text", null);

        if (str == null) {
            JSONObject obj = json.optJSONObject("text", null);

            if (obj != null) {
                str = obj.toString();
            }
        }

        if (str == null) {
            return Text.empty();
        }

        JsonElement src = JsonParser.parseString(str);

        return TextCodecs.CODEC.decode(registries.getOps(JsonOps.INSTANCE), src)
                .resultOrPartial()
                .map(Pair::getFirst)
                .orElse(Text.empty());
    }
}
