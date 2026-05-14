package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import work.lclpnet.lobby.game.api.prot.Protection;

public class WorldBlockProtection implements Protection<WorldBlockProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return (world, pos) -> exclude.isWithinScope(world, pos) && !include.isWithinScope(world, pos);
    }

    public interface Scope {

        boolean isWithinScope(Level world, BlockPos pos);
    }
}
