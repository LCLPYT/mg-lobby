package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.player.Player;

public interface PlayerGeneric2Scope<T, U> {

    PlayerGeneric2Scope<?, ?> CREATIVE_OP = (player, a, b) -> player.canUseGameMasterBlocks();

    boolean isWithinScope(Player player, T a, U b);

    @SuppressWarnings("unchecked")
    static <T, U> PlayerGeneric2Scope<T, U> creativeOp() {
        return (PlayerGeneric2Scope<T, U>) CREATIVE_OP;
    }
}
