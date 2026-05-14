package work.lclpnet.lobby.game.map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import work.lclpnet.kibu.hook.util.PositionRotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapUtils {

    private MapUtils() {}

    @NotNull
    public static Vec3 getSpawnPosition(GameMap gameMap) {
        if (gameMap.getProperty("spawn") instanceof JSONArray array) {
            return getSpawnVec3d(array);
        }

        throw missingProperty("spawn");
    }

    public static float getSpawnYaw(GameMap gameMap) {
        if (!(gameMap.getProperty("spawn-yaw") instanceof Number number)) return 0f;

        return getAngle(number);
    }

    @NotNull
    public static List<Vec3> getSpawnPositions(GameMap gameMap) {
        if (!(gameMap.getProperty("spawns") instanceof JSONArray array)) {
            throw missingProperty("spawns");
        }

        List<Vec3> spawns = new ArrayList<>();

        for (Object element : array) {
            if (element instanceof JSONArray elemArray) {
                spawns.add(getSpawnVec3d(elemArray));
            }
        }

        return spawns;
    }

    @NotNull
    public static List<PositionRotation> getSpawnPositionsAndRotation(GameMap gameMap) {
        if (!(gameMap.getProperty("spawns") instanceof JSONArray array)) {
            throw missingProperty("spawns");
        }

        List<PositionRotation> spawns = new ArrayList<>();

        for (Object element : array) {
            PositionRotation posRot = getPositionRotation(element);

            if (posRot == null) continue;

            spawns.add(posRot);
        }

        return spawns;
    }

    @NotNull
    public static Map<String, Vec3> getNamedSpawnPositions(GameMap gameMap) {
        if (!(gameMap.getProperty("spawns") instanceof JSONObject object)) {
            throw missingProperty("spawns");
        }

        Map<String, Vec3> spawns = new Object2ObjectOpenHashMap<>();

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

    @NotNull
    public static Map<String, PositionRotation> getNamedSpawnPositionsAndRotation(GameMap gameMap) {
        if (!(gameMap.getProperty("spawns") instanceof JSONObject object)) {
            throw missingProperty("spawns");
        }

        Map<String, PositionRotation> spawns = new Object2ObjectOpenHashMap<>();

        for (String key : object.keySet()) {
            Object value = object.get(key);

            PositionRotation posRot = getPositionRotation(value);

            if (posRot == null) continue;

            spawns.put(key, posRot);
        }

        return spawns;
    }

    private static PositionRotation getPositionRotation(Object value) {
        if (!(value instanceof JSONObject elemObj) || !elemObj.has("spawn")) return null;

        JSONArray array = elemObj.getJSONArray("spawn");
        Vec3 spawn = getSpawnVec3d(array);

        float yaw = 0, pitch = 0;

        if (elemObj.has("yaw")) {
            yaw = getAngle(elemObj.getNumber("yaw"));
        }

        if (elemObj.has("pitch")) {
            pitch = getAngle(elemObj.getNumber("pitch"));
        }

        return new PositionRotation(spawn.x, spawn.y, spawn.z, yaw, pitch);
    }

    @NotNull
    private static Vec3 getSpawnVec3d(JSONArray json) {
        if (json.length() < 3) {
            throw new IllegalArgumentException("JSONArray must have at least 3 elements");
        }

        return new Vec3(
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
        return Mth.wrapDegrees(number.floatValue());
    }

    private static IllegalStateException missingProperty(String property) {
        return new IllegalStateException("Property \"%s\" is undefined".formatted(property));
    }
}
