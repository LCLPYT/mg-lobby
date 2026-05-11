package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.world.entity.player.Player;
import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerIntBoolScope implements Scope<PlayerIntBoolScope.Check> {

    @Override
    public Check getGlobalScope() {
        return (_, _, _) -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return (player, i, b) -> exclude.isWithinScope(player, i, b) && !include.isWithinScope(player, i, b);
    }

    public interface Check {

        Check CREATIVE_OP = (player, _, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, int i, boolean b);
    }
}
