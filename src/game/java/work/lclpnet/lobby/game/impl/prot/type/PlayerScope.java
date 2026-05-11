package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.world.entity.player.Player;
import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerScope implements Scope<PlayerScope.Check> {

    @Override
    public Check getGlobalScope() {
        return _ -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return player -> exclude.isWithinScope(player) && !include.isWithinScope(player);
    }

    public interface Check {

        Check CREATIVE_OP = Player::canUseGameMasterBlocks;

        boolean isWithinScope(Player player);
    }
}
