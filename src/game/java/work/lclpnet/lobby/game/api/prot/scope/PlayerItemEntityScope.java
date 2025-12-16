package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public interface PlayerItemEntityScope {

    PlayerItemEntityScope CREATIVE_OP = (player, itemEntity) -> player.canUseGameMasterBlocks();

    boolean isWithinScope(Player player, ItemEntity itemEntity);
}
