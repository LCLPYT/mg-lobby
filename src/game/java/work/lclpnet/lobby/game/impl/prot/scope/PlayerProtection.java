package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.entity.player.Player;
import work.lclpnet.lobby.game.api.prot.Protection;

public class PlayerProtection implements Protection<PlayerProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return _ -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return player -> exclude.isWithinScope(player) && !include.isWithinScope(player);
    }

    public interface Scope {

        Scope CREATIVE_OP = Player::canUseGameMasterBlocks;

        boolean isWithinScope(Player player);
    }
}
