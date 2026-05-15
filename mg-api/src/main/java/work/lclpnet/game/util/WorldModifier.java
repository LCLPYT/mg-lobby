package work.lclpnet.game.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface WorldModifier {
    
    void setBlockState(BlockPos pos, BlockState state, int flags);

    void spawnEntity(Entity entity);

    default void setBlockState(BlockPos pos, BlockState state) {
        setBlockState(pos, state, Block.UPDATE_ALL);
    }
}
