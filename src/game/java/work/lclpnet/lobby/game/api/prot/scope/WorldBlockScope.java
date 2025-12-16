package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface WorldBlockScope {

    boolean isWithinScope(Level world, BlockPos pos);
}
