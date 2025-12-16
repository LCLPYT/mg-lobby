package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface WorldBlockItemStackScope {

    boolean isWithinScope(Level world, BlockPos pos, ItemStack stack);
}
