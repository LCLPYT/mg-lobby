package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.entity.player.Player;
import work.lclpnet.lobby.game.api.prot.Protection;

public class PlayerGeneric2Protection<T, U> implements Protection<PlayerGeneric2Protection.Scope<T, U>> {

    @Override
    public Scope<T, U> getGlobalScope() {
        return (_, _, _) -> true;
    }

    @Override
    public Scope<T, U> getCombinedExcludeScope(Scope<T, U> exclude, Scope<T, U> include) {
        return (player, a, b) -> exclude.isWithinScope(player, a, b) && !include.isWithinScope(player, a, b);
    }

    public interface Scope<T, U> {

        Scope<?, ?> CREATIVE_OP = (player, _, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, T a, U b);

        @SuppressWarnings("unchecked")
        static <T, U> Scope<T, U> creativeOp() {
            return (Scope<T, U>) CREATIVE_OP;
        }
    }
}
