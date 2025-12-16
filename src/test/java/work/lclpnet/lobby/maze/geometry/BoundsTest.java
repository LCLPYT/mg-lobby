package work.lclpnet.lobby.maze.geometry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import work.lclpnet.lobby.decor.maze.geometry.Bounds;

import static org.junit.jupiter.api.Assertions.*;

class BoundsTest {

    @Test
    void getMin() {
        var bounds = new Bounds(new BlockPos(0, 0, 0), new BlockPos(1, 1, 1));
        assertEquals(new Vec3(0, 0, 0), bounds.getMin());

        bounds = new Bounds(new BlockPos(1, 1, 1), new BlockPos(0, 0, 0));
        assertEquals(new Vec3(0, 0, 0), bounds.getMin());

        bounds = new Bounds(new BlockPos(0, 1, 1), new BlockPos(1, 0, 0));
        assertEquals(new Vec3(0, 0, 0), bounds.getMin());

        bounds = new Bounds(new BlockPos(1, 0, 0), new BlockPos(0, 0, 1));
        assertEquals(new Vec3(0, 0, 0), bounds.getMin());
    }

    @Test
    void getMax() {
        var bounds = new Bounds(new BlockPos(0, 0, 0), new BlockPos(1, 1, 1));
        assertEquals(new Vec3(1, 1, 1), bounds.getMax());

        bounds = new Bounds(new BlockPos(1, 1, 1), new BlockPos(0, 0, 0));
        assertEquals(new Vec3(1, 1, 1), bounds.getMax());

        bounds = new Bounds(new BlockPos(0, 1, 1), new BlockPos(1, 0, 0));
        assertEquals(new Vec3(1, 1, 1), bounds.getMax());

        bounds = new Bounds(new BlockPos(1, 0, 0), new BlockPos(0, 0, 1));
        assertEquals(new Vec3(1, 0, 1), bounds.getMax());
    }

    @Test
    void contains() {
        final var bounds = new Bounds(new BlockPos(0, 0, 0), new BlockPos(1, 1, 1));

        assertTrue(bounds.contains(new Vec3(0, 0, 0)));
        assertTrue(bounds.contains(new Vec3(1, 0, 0)));
        assertTrue(bounds.contains(new Vec3(0, 1, 0)));
        assertTrue(bounds.contains(new Vec3(0, 0, 1)));
        assertTrue(bounds.contains(new Vec3(1, 1, 0)));
        assertTrue(bounds.contains(new Vec3(0, 1, 1)));
        assertTrue(bounds.contains(new Vec3(1, 0, 1)));
        assertTrue(bounds.contains(new Vec3(1, 1, 1)));
        assertTrue(bounds.contains(new Vec3(0.5, 0.5, 0.5)));
        assertTrue(bounds.contains(new Vec3(0.05, 0.95, 0.66666)));

        assertFalse(bounds.contains(new Vec3(-1, 0, 0)));
        assertFalse(bounds.contains(new Vec3(-1, 0, -1)));
        assertFalse(bounds.contains(new Vec3(0, 1.1, 0)));
        assertFalse(bounds.contains(new Vec3(0, 1, 1.0000001)));
    }
}