package work.lclpnet.lobby.config;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.json.JSONArray;

public class ConfigUtil {

    private ConfigUtil() {}

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

    public static Vec3 getVec3d(JSONArray json) {
        if (json.length() < 3) throw new IllegalArgumentException("JSONArray must have at least 3 elements");

        return new Vec3(
                json.getDouble(0),
                json.getDouble(1),
                json.getDouble(2)
        );
    }

    public static JSONArray writeVec3d(Vec3 pos) {
        JSONArray array = new JSONArray();

        array.put(pos.x());
        array.put(pos.y());
        array.put(pos.z());

        return array;
    }

    public static float readFloat(Number number) {
        return number.floatValue();
    }

    public static float readAngle(Number number) {
        return Mth.wrapDegrees(readFloat(number));
    }

    public static Vec3 readVec3d(JSONArray tuple) {
        if (tuple.length() < 3) throw new IllegalArgumentException("Tuple must be of size 3");

        return new Vec3(tuple.getDouble(0), tuple.getDouble(1), tuple.getDouble(2));
    }
}
