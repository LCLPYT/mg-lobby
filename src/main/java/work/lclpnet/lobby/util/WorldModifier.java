package work.lclpnet.lobby.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface WorldModifier {
    
    void setBlockState(BlockPos pos, BlockState state);

    void spawnEntity(Entity entity);
}
