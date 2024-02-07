package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface PlayerItemStackScope {

    PlayerItemStackScope CREATIVE_OP = (player, stack) -> player.isCreativeLevelTwoOp();

    boolean isWithinScope(PlayerEntity player, ItemStack stack);
}
