package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerItemEntityScope implements Scope<PlayerItemEntityScope.Check> {

    @Override
    public Check getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return (player, itemEntity) -> exclude.isWithinScope(player, itemEntity) && !include.isWithinScope(player, itemEntity);
    }

    public interface Check {

        Check CREATIVE_OP = (player, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, ItemEntity itemEntity);
    }
}
