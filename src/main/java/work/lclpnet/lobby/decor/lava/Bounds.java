package work.lclpnet.lobby.decor.lava;

import org.json.JSONArray;

public record Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    public boolean contains(double x, double y, double z) {
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    public JSONArray asJson() {
        JSONArray tuple = new JSONArray();
        JSONArray first = new JSONArray();
        JSONArray second = new JSONArray();

        first.put(minX);
        first.put(minY);
        first.put(minZ);

        second.put(maxX);
        second.put(maxY);
        second.put(maxZ);

        tuple.put(first);
        tuple.put(second);

        return tuple;
    }

    public static Bounds ofChecked(int x1, int x2, int y1, int y2, int z1, int z2) {
        return new Bounds(
                Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)
        );
    }

    public static Bounds parse(JSONArray tuple) {
        if (tuple.length() < 2) throw new IllegalArgumentException("Tuple must be of size 2");

        JSONArray first = tuple.getJSONArray(0);
        JSONArray second = tuple.getJSONArray(1);

        if (first.length() < 3) throw new IllegalArgumentException("First tuple element must be of size 3");
        if (second.length() < 3) throw new IllegalArgumentException("Second tuple element must be of size 3");

        int x1 = first.getInt(0), y1 = first.getInt(1), z1 = first.getInt(2);
        int x2 = second.getInt(0), y2 = second.getInt(1), z2 = second.getInt(2);

        return Bounds.ofChecked(x1, x2, y1, y2, z1, z2);
    }
}
