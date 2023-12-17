package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public interface PlayerEntityScope<E extends Entity> {

    PlayerEntityScope<?> CREATIVE_OP = (player, entity) -> player.isCreativeLevelTwoOp();

    boolean isWithinScope(PlayerEntity player, E entity);

    @SuppressWarnings("unchecked")
    static <E extends Entity> PlayerEntityScope<E> creativeOp() {
        return (PlayerEntityScope<E>) CREATIVE_OP;
    }
}
