package work.lclpnet.lobby.maze.geometry;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoundsTest {

    @Test
    void getMin() {
        var bounds = new Bounds(new BlockPos(0, 0, 0), new BlockPos(1, 1, 1));
        assertEquals(new Vec3d(0, 0, 0), bounds.getMin());

        bounds = new Bounds(new BlockPos(1, 1, 1), new BlockPos(0, 0, 0));
        assertEquals(new Vec3d(0, 0, 0), bounds.getMin());

        bounds = new Bounds(new BlockPos(0, 1, 1), new BlockPos(1, 0, 0));
        assertEquals(new Vec3d(0, 0, 0), bounds.getMin());

        bounds = new Bounds(new BlockPos(1, 0, 0), new BlockPos(0, 0, 1));
        assertEquals(new Vec3d(0, 0, 0), bounds.getMin());
    }

    @Test
    void getMax() {
        var bounds = new Bounds(new BlockPos(0, 0, 0), new BlockPos(1, 1, 1));
        assertEquals(new Vec3d(1, 1, 1), bounds.getMax());

        bounds = new Bounds(new BlockPos(1, 1, 1), new BlockPos(0, 0, 0));
        assertEquals(new Vec3d(1, 1, 1), bounds.getMax());

        bounds = new Bounds(new BlockPos(0, 1, 1), new BlockPos(1, 0, 0));
        assertEquals(new Vec3d(1, 1, 1), bounds.getMax());

        bounds = new Bounds(new BlockPos(1, 0, 0), new BlockPos(0, 0, 1));
        assertEquals(new Vec3d(1, 0, 1), bounds.getMax());
    }

    @Test
    void contains() {
        final var bounds = new Bounds(new BlockPos(0, 0, 0), new BlockPos(1, 1, 1));

        assertTrue(bounds.contains(new Vec3d(0, 0, 0)));
        assertTrue(bounds.contains(new Vec3d(1, 0, 0)));
        assertTrue(bounds.contains(new Vec3d(0, 1, 0)));
        assertTrue(bounds.contains(new Vec3d(0, 0, 1)));
        assertTrue(bounds.contains(new Vec3d(1, 1, 0)));
        assertTrue(bounds.contains(new Vec3d(0, 1, 1)));
        assertTrue(bounds.contains(new Vec3d(1, 0, 1)));
        assertTrue(bounds.contains(new Vec3d(1, 1, 1)));
        assertTrue(bounds.contains(new Vec3d(0.5, 0.5, 0.5)));
        assertTrue(bounds.contains(new Vec3d(0.05, 0.95, 0.66666)));

        assertFalse(bounds.contains(new Vec3d(-1, 0, 0)));
        assertFalse(bounds.contains(new Vec3d(-1, 0, -1)));
        assertFalse(bounds.contains(new Vec3d(0, 1.1, 0)));
        assertFalse(bounds.contains(new Vec3d(0, 1, 1.0000001)));
    }
}