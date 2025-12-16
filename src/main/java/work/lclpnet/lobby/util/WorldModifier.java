package work.lclpnet.lobby.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

public interface WorldModifier {
    
    void setBlockState(BlockPos pos, BlockState state, int flags);

    void spawnEntity(Entity entity);

    default void setBlockState(BlockPos pos, BlockState state) {
        setBlockState(pos, state, Block.UPDATE_ALL);
    }
}
