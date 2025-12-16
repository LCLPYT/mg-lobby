package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.player.Player;

public interface PlayerGenericScope<T> {

    PlayerGenericScope<?> CREATIVE_OP = (player, obj) -> player.canUseGameMasterBlocks();

    boolean isWithinScope(Player player, T obj);

    @SuppressWarnings("unchecked")
    static <T> PlayerGenericScope<T> creativeOp() {
        return (PlayerGenericScope<T>) CREATIVE_OP;
    }
}
