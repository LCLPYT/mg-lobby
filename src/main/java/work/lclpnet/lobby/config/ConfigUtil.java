package work.lclpnet.lobby.config;

import net.minecraft.util.math.BlockPos;
import org.json.JSONArray;

public class ConfigUtil {

    public static BlockPos getBlockPos(JSONArray json) {
        if (json.length() < 3) throw new IllegalArgumentException("JSONArray must have at least 3 elements");

        return new BlockPos(
                json.getInt(0),
                json.getInt(1),
                json.getInt(2)
        );
    }

    public static JSONArray writeBlockPos(BlockPos start) {
        JSONArray array = new JSONArray();

        array.put(start.getX());
        array.put(start.getY());
        array.put(start.getZ());

        return array;
    }
}
