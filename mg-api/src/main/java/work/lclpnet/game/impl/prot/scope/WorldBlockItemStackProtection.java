package work.lclpnet.game.impl.prot.scope;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import work.lclpnet.game.api.prot.Protection;

public class WorldBlockItemStackProtection implements Protection<WorldBlockItemStackProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return (_, _, _) -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return (world, pos, stack) -> exclude.isWithinScope(world, pos, stack) && !include.isWithinScope(world, pos, stack);
    }

    public interface Scope {

        boolean isWithinScope(Level world, BlockPos pos, ItemStack stack);
    }
}
