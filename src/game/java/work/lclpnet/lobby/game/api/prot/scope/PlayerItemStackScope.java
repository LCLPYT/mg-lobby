package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface PlayerItemStackScope {

    PlayerItemStackScope CREATIVE_OP = (player, stack) -> player.canUseGameMasterBlocks();

    boolean isWithinScope(Player player, ItemStack stack);
}
