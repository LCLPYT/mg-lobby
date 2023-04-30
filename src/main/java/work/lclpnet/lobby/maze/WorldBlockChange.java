package work.lclpnet.lobby.maze;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class WorldBlockChange implements BlockStateWriter {

    private final World world;
    private final Map<BlockPos, BlockState> states = new HashMap<>();

    public WorldBlockChange(World world) {
        this.world = world;
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
        synchronized (this) {
            if (!states.containsKey(pos)) {
                BlockState prevState = world.getBlockState(pos);

                if (prevState != state) {
                    states.put(new BlockPos(pos), prevState);
                }
            }
        }

        world.setBlockState(pos, state);
    }

    public void undo() {
        synchronized (this) {
            for (var entry : states.entrySet()) {
                world.setBlockState(entry.getKey(), entry.getValue());
            }

            states.clear();
        }
    }
}
