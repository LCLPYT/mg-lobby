package work.lclpnet.lobby.decor.jnr;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface PosGenerator {

    @Nullable
    BlockPos generate();

    void reset();
}
