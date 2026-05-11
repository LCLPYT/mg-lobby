package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.entity.player.Player;
import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerGenericScope<T> implements Scope<PlayerGenericScope.Check<T>> {

    @Override
    public Check<T> getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Check<T> getResultingScope(Check<T> exclude, Check<T> include) {
        return (player, obj) -> exclude.isWithinScope(player, obj) && !include.isWithinScope(player, obj);
    }

    public interface Check<T> {

        Check<?> CREATIVE_OP = (player, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, T obj);

        @SuppressWarnings("unchecked")
        static <T> Check<T> creativeOp() {
            return (Check<T>) CREATIVE_OP;
        }
    }
}
