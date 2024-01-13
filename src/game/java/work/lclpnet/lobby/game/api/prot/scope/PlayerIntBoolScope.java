package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.entity.player.PlayerEntity;

public interface PlayerIntBoolScope {

    PlayerIntBoolScope CREATIVE_OP = (player, i, b) -> player.isCreativeLevelTwoOp();

    boolean isWithinScope(PlayerEntity player, int i, boolean b);
}
