package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface WorldBlockScope {

    boolean isWithinScope(World world, BlockPos pos);
}
