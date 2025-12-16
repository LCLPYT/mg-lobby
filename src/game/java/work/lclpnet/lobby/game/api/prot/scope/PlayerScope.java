package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.player.Player;

public interface PlayerScope {

    PlayerScope CREATIVE_OP = Player::canUseGameMasterBlocks;

    boolean isWithinScope(Player player);
}
