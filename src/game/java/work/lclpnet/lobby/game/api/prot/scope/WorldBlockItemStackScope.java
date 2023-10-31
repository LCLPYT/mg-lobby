package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface WorldBlockItemStackScope {

    boolean isWithinScope(World world, BlockPos pos, ItemStack stack);
}
