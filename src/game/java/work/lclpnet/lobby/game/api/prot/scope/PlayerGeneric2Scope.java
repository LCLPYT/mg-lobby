package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.entity.player.PlayerEntity;

public interface PlayerGeneric2Scope<T, U> {

    PlayerGeneric2Scope<?, ?> CREATIVE_OP = (player, a, b) -> player.isCreativeLevelTwoOp();

    boolean isWithinScope(PlayerEntity player, T a, U b);

    @SuppressWarnings("unchecked")
    static <T, U> PlayerGeneric2Scope<T, U> creativeOp() {
        return (PlayerGeneric2Scope<T, U>) CREATIVE_OP;
    }
}
