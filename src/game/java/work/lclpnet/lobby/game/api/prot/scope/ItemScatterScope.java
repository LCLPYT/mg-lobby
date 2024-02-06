package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface ItemScatterScope {

    boolean isWithinScope(World world, double x, double y, double z, ItemStack stack);
}
