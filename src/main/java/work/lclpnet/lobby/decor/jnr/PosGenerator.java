package work.lclpnet.lobby.decor.jnr;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface PosGenerator {

    @Nullable
    BlockPos generate();

    void reset();
}
