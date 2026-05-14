package work.lclpnet.game.impl.prot.scope;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import work.lclpnet.game.api.prot.Protection;

public class PlayerItemStackProtection implements Protection<PlayerItemStackProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return (player, stack) -> exclude.isWithinScope(player, stack) && !include.isWithinScope(player, stack);
    }

    public interface Scope {

        Scope CREATIVE_OP = (player, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, ItemStack stack);
    }
}
