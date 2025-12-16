package work.lclpnet.lobby.decor.greet;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import work.lclpnet.lobby.config.ConfigUtil;

public record GreetingConfig(Vec3 pos, float scale, float rotationY, Component text) {

    public JSONObject asJson(HolderLookup.Provider registries) {
        JSONObject json = new JSONObject();

        json.put("position", ConfigUtil.writeVec3d(pos));
        json.put("scale", scale);
        json.put("rotation_y", rotationY);

        encodeText(registries, json);

        return json;
    }

    public static GreetingConfig parse(JSONObject json, HolderLookup.Provider registries) {
        Vec3 position = ConfigUtil.readVec3d(json.getJSONArray("position"));
        float scale = ConfigUtil.readFloat(json.getNumber("scale"));
        float rotationY = ConfigUtil.readAngle(json.getNumber("rotation_y"));

        Component text = decodeText(json, registries);

        return new GreetingConfig(position, scale, rotationY, text);
    }

    private void encodeText(HolderLookup.Provider registries, JSONObject json) {
        String textJson = ComponentSerialization.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), text)
                .resultOrPartial()
                .map(JsonElement::toString)
                .orElse("");

        json.put("text", new JSONObject(textJson));
    }

    private static @NotNull Component decodeText(JSONObject json, HolderLookup.Provider registries) {
        String str = json.optString("text", null);

        if (str == null) {
            JSONObject obj = json.optJSONObject("text", null);

            if (obj != null) {
                str = obj.toString();
            }
        }

        if (str == null) {
            return Component.empty();
        }

        JsonElement src = JsonParser.parseString(str);

        return ComponentSerialization.CODEC.decode(registries.createSerializationContext(JsonOps.INSTANCE), src)
                .resultOrPartial()
                .map(Pair::getFirst)
                .orElse(Component.empty());
    }
}
