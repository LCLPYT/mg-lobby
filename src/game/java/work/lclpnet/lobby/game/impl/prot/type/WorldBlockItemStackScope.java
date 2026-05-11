package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import work.lclpnet.lobby.game.api.prot.Scope;

public class WorldBlockItemStackScope implements Scope<WorldBlockItemStackScope.Check> {

    @Override
    public Check getGlobalScope() {
        return (_, _, _) -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return (world, pos, stack) -> exclude.isWithinScope(world, pos, stack) && !include.isWithinScope(world, pos, stack);
    }

    public interface Check {

        boolean isWithinScope(Level world, BlockPos pos, ItemStack stack);
    }
}
