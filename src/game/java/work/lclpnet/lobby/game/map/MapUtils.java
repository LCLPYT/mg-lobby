package work.lclpnet.lobby.game.map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.json.JSONArray;
import org.json.JSONObject;
import work.lclpnet.kibu.hook.util.PositionRotation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapUtils {

    private MapUtils() {}

    @Nonnull
    public static Vec3d getSpawnPosition(GameMap gameMap) {
        if (gameMap.getProperty("spawn") instanceof JSONArray array) {
            return getSpawnVec3d(array);
        }

        throw missingProperty("spawn");
    }

    public static float getSpawnYaw(GameMap gameMap) {
        if (!(gameMap.getProperty("spawn-yaw") instanceof Number number)) return 0f;

        return getAngle(number);
    }

    @Nonnull
    public static List<Vec3d> getSpawnPositions(GameMap gameMap) {
        if (!(gameMap.getProperty("spawns") instanceof JSONArray array)) {
            throw missingProperty("spawns");
        }

        List<Vec3d> spawns = new ArrayList<>();

        for (Object element : array) {
            if (element instanceof JSONArray elemArray) {
                spawns.add(getSpawnVec3d(elemArray));
            }
        }

        return spawns;
    }

    @Nonnull
    public static Map<String, Vec3d> getNamedSpawnPositions(GameMap gameMap) {
        if (!(gameMap.getProperty("spawns") instanceof JSONObject object)) {
            throw missingProperty("spawns");
        }

        Map<String, Vec3d> spawns = new Object2ObjectOpenHashMap<>();

        for (String key : object.keySet()) {
            Object value = object.get(key);

            if (value instanceof JSONArray elemArray) {
                spawns.put(key, getSpawnVec3d(elemArray));
                continue;
            }

            if (value instanceof JSONObject elemObj) {
                if (!elemObj.has("spawn")) continue;

                JSONArray array = elemObj.getJSONArray("spawn");
                spawns.put(key, getSpawnVec3d(array));
            }
        }

        return spawns;
    }

    @Nonnull
    public static Map<String, PositionRotation> getNamedSpawnPositionsAndRotation(GameMap gameMap) {
        if (!(gameMap.getProperty("spawns") instanceof JSONObject object)) {
            throw missingProperty("spawns");
        }

        Map<String, PositionRotation> spawns = new Object2ObjectOpenHashMap<>();

        for (String key : object.keySet()) {
            Object value = object.get(key);

            if (value instanceof JSONObject elemObj) {
                if (!elemObj.has("spawn")) continue;

                JSONArray array = elemObj.getJSONArray("spawn");
                Vec3d spawn = getSpawnVec3d(array);

                float yaw = 0, pitch = 0;

                if (elemObj.has("yaw")) {
                    yaw = getAngle(elemObj.getNumber("yaw"));
                }

                if (elemObj.has("pitch")) {
                    pitch = getAngle(elemObj.getNumber("pitch"));
                }

                spawns.put(key, new PositionRotation(spawn.x, spawn.y, spawn.z, yaw, pitch));
            }
        }

        return spawns;
    }

    @Nonnull
    private static Vec3d getSpawnVec3d(JSONArray json) {
        if (json.length() < 3) {
            throw new IllegalArgumentException("JSONArray must have at least 3 elements");
        }

        return new Vec3d(
                getSpawnDouble(json, 0),
                json.getDouble(1),  // do not center y
                getSpawnDouble(json, 2)
        );
    }

    private static double getSpawnDouble(JSONArray json, int index) {
        double d = json.getDouble(index);
        int i = json.getInt(index);

        if (Math.abs(d - i) < 1e-6) {
            // return centered block pos
            return i + 0.5;
        }

        return d;
    }

    private static float getAngle(Number number) {
        return MathHelper.wrapDegrees(number.floatValue());
    }

    private static IllegalStateException missingProperty(String property) {
        return new IllegalStateException("Property \"%s\" is undefined".formatted(property));
    }
}
