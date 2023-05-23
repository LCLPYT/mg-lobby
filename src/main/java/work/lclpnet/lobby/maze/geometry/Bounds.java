package work.lclpnet.lobby.maze.geometry;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Bounds {

    private final double minX, minY, minZ;
    private final double maxX, maxY, maxZ;

    public Bounds(Vec3i from, Vec3i to) {
        this(Vec3d.of(from), Vec3d.of(to));
    }

    public Bounds(Vec3d from, Vec3d to) {
        this.minX = min(from.getX(), to.getX());
        this.minY = min(from.getY(), to.getY());
        this.minZ = min(from.getZ(), to.getZ());
        this.maxX = max(from.getX(), to.getX());
        this.maxY = max(from.getY(), to.getY());
        this.maxZ = max(from.getZ(), to.getZ());
    }

    public Vec3d getMin() {
        return new Vec3d(minX, minY, minZ);
    }

    public Vec3d getMax() {
        return new Vec3d(maxX, maxY, maxZ);
    }

    public boolean contains(Vec3d pos) {
        return pos.getX() >= minX && pos.getX() <= maxX &&
                pos.getY() >= minY && pos.getY() <= maxY &&
                pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    public boolean contains(Vec3i pos) {
        return pos.getX() >= minX && pos.getX() <= maxX &&
                pos.getY() >= minY && pos.getY() <= maxY &&
                pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }
}
