package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.entity.player.PlayerEntity;

public interface PlayerScope {

    PlayerScope CREATIVE_OP = PlayerEntity::isCreativeLevelTwoOp;

    boolean isWithinScope(PlayerEntity player);
}
