package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.world.entity.player.Player;
import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerGeneric2Scope<T, U> implements Scope<PlayerGeneric2Scope.Check<T, U>> {

    @Override
    public Check<T, U> getGlobalScope() {
        return (_, _, _) -> true;
    }

    @Override
    public Check<T, U> getResultingScope(Check<T, U> exclude, Check<T, U> include) {
        return (player, a, b) -> exclude.isWithinScope(player, a, b) && !include.isWithinScope(player, a, b);
    }

    public interface Check<T, U> {

        Check<?, ?> CREATIVE_OP = (player, _, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, T a, U b);

        @SuppressWarnings("unchecked")
        static <T, U> Check<T, U> creativeOp() {
            return (Check<T, U>) CREATIVE_OP;
        }
    }
}
