package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.entity.player.Player;
import work.lclpnet.lobby.game.api.prot.Protection;

public class PlayerIntProtection implements Protection<PlayerIntProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return (player, i) -> exclude.isWithinScope(player, i) && !include.isWithinScope(player, i);
    }

    public interface Scope {

        Scope CREATIVE_OP = (player, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, int i);
    }
}
