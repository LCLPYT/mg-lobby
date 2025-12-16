package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface PlayerEntityScope<E extends Entity> extends PlayerGenericScope<E> {

    PlayerEntityScope<?> CREATIVE_OP = (player, entity) -> player.canUseGameMasterBlocks();

    boolean isWithinScope(Player player, E entity);

    @SuppressWarnings("unchecked")
    static <E extends Entity> PlayerEntityScope<E> creativeOp() {
        return (PlayerEntityScope<E>) CREATIVE_OP;
    }
}
