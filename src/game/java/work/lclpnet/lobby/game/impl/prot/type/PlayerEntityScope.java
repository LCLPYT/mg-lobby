package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerEntityScope<E extends Entity> implements Scope<PlayerEntityScope.Check<E>> {

    @Override
    public Check<E> getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Check<E> getResultingScope(Check<E> exclude, Check<E> include) {
        return (player, entity) -> exclude.isWithinScope(player, entity) && !include.isWithinScope(player, entity);
    }

    public interface Check<E extends Entity> {

        Check<?> CREATIVE_OP = (player, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, E entity);

        @SuppressWarnings("unchecked")
        static <E extends Entity> Check<E> creativeOp() {
            return (Check<E>) CREATIVE_OP;
        }
    }
}
