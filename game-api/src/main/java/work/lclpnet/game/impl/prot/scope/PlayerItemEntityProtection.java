package work.lclpnet.game.impl.prot.scope;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import work.lclpnet.game.api.prot.Protection;

public class PlayerItemEntityProtection implements Protection<PlayerItemEntityProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return (player, itemEntity) -> exclude.isWithinScope(player, itemEntity) && !include.isWithinScope(player, itemEntity);
    }

    public interface Scope {

        Scope CREATIVE_OP = (player, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, ItemEntity itemEntity);
    }
}
