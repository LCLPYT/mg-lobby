package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

public interface PlayerItemEntityScope {

    PlayerItemEntityScope CREATIVE_OP = (player, itemEntity) -> player.isCreativeLevelTwoOp();

    boolean isWithinScope(PlayerEntity player, ItemEntity itemEntity);
}
