package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.player.Player;

public interface PlayerIntBoolScope {

    PlayerIntBoolScope CREATIVE_OP = (player, i, b) -> player.canUseGameMasterBlocks();

    boolean isWithinScope(Player player, int i, boolean b);
}
