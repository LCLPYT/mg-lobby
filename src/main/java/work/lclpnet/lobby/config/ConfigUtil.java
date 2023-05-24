package work.lclpnet.lobby.config;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

    public static JSONArray writeBlockPos(BlockPos pos) {
        JSONArray array = new JSONArray();

        array.put(pos.getX());
        array.put(pos.getY());
        array.put(pos.getZ());

        return array;
    }

    public static Vec3d getVec3d(JSONArray json) {
        if (json.length() < 3) throw new IllegalArgumentException("JSONArray must have at least 3 elements");

        return new Vec3d(
                json.getDouble(0),
                json.getDouble(1),
                json.getDouble(2)
        );
    }

    public static JSONArray writeVec3d(Vec3d pos) {
        JSONArray array = new JSONArray();

        array.put(pos.getX());
        array.put(pos.getY());
        array.put(pos.getZ());

        return array;
    }
}
