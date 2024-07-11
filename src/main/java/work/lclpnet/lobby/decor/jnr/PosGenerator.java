package work.lclpnet.lobby.decor.jnr;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface PosGenerator {

    @Nullable
    BlockPos generate();

    void reset();
}
