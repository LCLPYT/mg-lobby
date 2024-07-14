package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.entity.player.PlayerEntity;

public interface PlayerGenericScope<T> {

    PlayerGenericScope<?> CREATIVE_OP = (player, obj) -> player.isCreativeLevelTwoOp();

    boolean isWithinScope(PlayerEntity player, T obj);

    @SuppressWarnings("unchecked")
    static <T> PlayerGenericScope<T> creativeOp() {
        return (PlayerGenericScope<T>) CREATIVE_OP;
    }
}
