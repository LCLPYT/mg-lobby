package work.lclpnet.lobby.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface WorldModifier {
    
    void setBlockState(BlockPos pos, BlockState state, int flags);

    void spawnEntity(Entity entity);

    default void setBlockState(BlockPos pos, BlockState state) {
        setBlockState(pos, state, Block.NOTIFY_ALL);
    }
}
