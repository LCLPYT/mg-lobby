package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.entity.player.Player;
import work.lclpnet.lobby.game.api.prot.Protection;

public class PlayerIntBoolProtection implements Protection<PlayerIntBoolProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return (_, _, _) -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return (player, i, b) -> exclude.isWithinScope(player, i, b) && !include.isWithinScope(player, i, b);
    }

    public interface Scope {

        Scope CREATIVE_OP = (player, _, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, int i, boolean b);
    }
}
