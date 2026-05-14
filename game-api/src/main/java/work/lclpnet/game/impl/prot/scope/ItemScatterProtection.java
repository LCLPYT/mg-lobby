package work.lclpnet.game.impl.prot.scope;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import work.lclpnet.game.api.prot.Protection;

public class ItemScatterProtection implements Protection<ItemScatterProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return (_, _, _, _, _) -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return (world, x, y, z, stack) -> exclude.isWithinScope(world, x, y, z, stack) && !include.isWithinScope(world, x, y, z, stack);
    }

    public interface Scope {

        boolean isWithinScope(Level world, double x, double y, double z, ItemStack stack);
    }
}
