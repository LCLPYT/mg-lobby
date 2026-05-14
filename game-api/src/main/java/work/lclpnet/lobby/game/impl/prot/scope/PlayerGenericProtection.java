package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.entity.player.Player;
import work.lclpnet.lobby.game.api.prot.Protection;

public class PlayerGenericProtection<T> implements Protection<PlayerGenericProtection.Scope<T>> {

    @Override
    public Scope<T> getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Scope<T> getCombinedExcludeScope(Scope<T> exclude, Scope<T> include) {
        return (player, obj) -> exclude.isWithinScope(player, obj) && !include.isWithinScope(player, obj);
    }

    public interface Scope<T> {

        Scope<?> CREATIVE_OP = (player, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, T obj);

        @SuppressWarnings("unchecked")
        static <T> Scope<T> creativeOp() {
            return (Scope<T>) CREATIVE_OP;
        }
    }
}
