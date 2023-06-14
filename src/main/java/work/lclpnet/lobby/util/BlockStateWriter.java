package work.lclpnet.lobby.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface BlockStateWriter {
    
    void setBlockState(BlockPos pos, BlockState state);
}
