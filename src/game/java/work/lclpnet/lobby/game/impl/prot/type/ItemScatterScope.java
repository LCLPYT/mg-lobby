package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import work.lclpnet.lobby.game.api.prot.Scope;

public class ItemScatterScope implements Scope<ItemScatterScope.Check> {

    @Override
    public Check getGlobalScope() {
        return (_, _, _, _, _) -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return (world, x, y, z, stack) -> exclude.isWithinScope(world, x, y, z, stack) && !include.isWithinScope(world, x, y, z, stack);
    }

    public interface Check {

        boolean isWithinScope(Level world, double x, double y, double z, ItemStack stack);
    }
}
