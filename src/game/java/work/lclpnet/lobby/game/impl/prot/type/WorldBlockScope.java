package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import work.lclpnet.lobby.game.api.prot.Scope;

public class WorldBlockScope implements Scope<WorldBlockScope.Check> {

    @Override
    public Check getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return (world, pos) -> exclude.isWithinScope(world, pos) && !include.isWithinScope(world, pos);
    }

    public interface Check {

        boolean isWithinScope(Level world, BlockPos pos);
    }
}
