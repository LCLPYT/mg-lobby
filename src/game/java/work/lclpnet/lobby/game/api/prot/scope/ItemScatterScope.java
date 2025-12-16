package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ItemScatterScope {

    boolean isWithinScope(Level world, double x, double y, double z, ItemStack stack);
}
