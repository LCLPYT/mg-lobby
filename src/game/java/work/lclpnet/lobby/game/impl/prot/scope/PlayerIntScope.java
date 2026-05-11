package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.entity.player.Player;
import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerIntScope implements Scope<PlayerIntScope.Check> {

    @Override
    public Check getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return (player, i) -> exclude.isWithinScope(player, i) && !include.isWithinScope(player, i);
    }

    public interface Check {

        Check CREATIVE_OP = (player, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, int i);
    }
}
