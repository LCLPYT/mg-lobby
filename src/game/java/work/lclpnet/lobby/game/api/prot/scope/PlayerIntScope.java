package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.player.Player;

public interface PlayerIntScope {

    PlayerIntScope CREATIVE_OP = (player, i) -> player.canUseGameMasterBlocks();

    boolean isWithinScope(Player player, int i);
}
