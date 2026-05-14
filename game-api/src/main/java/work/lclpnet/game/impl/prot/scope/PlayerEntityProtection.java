package work.lclpnet.game.impl.prot.scope;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import work.lclpnet.game.api.prot.Protection;

public class PlayerEntityProtection<E extends Entity> implements Protection<PlayerEntityProtection.Scope<E>> {

    @Override
    public Scope<E> getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Scope<E> getCombinedExcludeScope(Scope<E> exclude, Scope<E> include) {
        return (player, entity) -> exclude.isWithinScope(player, entity) && !include.isWithinScope(player, entity);
    }

    public interface Scope<E extends Entity> {

        Scope<?> CREATIVE_OP = (player, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, E entity);

        @SuppressWarnings("unchecked")
        static <E extends Entity> Scope<E> creativeOp() {
            return (Scope<E>) CREATIVE_OP;
        }
    }
}
