package work.lclpnet.lobby.decor.maze.geometry;

import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Bounds {

    private final double minX, minY, minZ;
    private final double maxX, maxY, maxZ;

    public Bounds(Vec3i from, Vec3i to) {
        this(Vec3.atLowerCornerOf(from), Vec3.atLowerCornerOf(to));
    }

    public Bounds(Vec3 from, Vec3 to) {
        this.minX = min(from.x(), to.x());
        this.minY = min(from.y(), to.y());
        this.minZ = min(from.z(), to.z());
        this.maxX = max(from.x(), to.x());
        this.maxY = max(from.y(), to.y());
        this.maxZ = max(from.z(), to.z());
    }

    public Vec3 getMin() {
        return new Vec3(minX, minY, minZ);
    }

    public Vec3 getMax() {
        return new Vec3(maxX, maxY, maxZ);
    }

    public boolean contains(Vec3 pos) {
        return pos.x() >= minX && pos.x() <= maxX &&
                pos.y() >= minY && pos.y() <= maxY &&
                pos.z() >= minZ && pos.z() <= maxZ;
    }

    public boolean contains(Vec3i pos) {
        return pos.getX() >= minX && pos.getX() <= maxX &&
                pos.getY() >= minY && pos.getY() <= maxY &&
                pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }
}
