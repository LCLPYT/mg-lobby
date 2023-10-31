package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.entity.player.PlayerEntity;

public interface PlayerIntScope {

    PlayerIntScope CREATIVE_OP = (player, i) -> player.isCreativeLevelTwoOp();

    boolean isWithinScope(PlayerEntity player, int i);
}
