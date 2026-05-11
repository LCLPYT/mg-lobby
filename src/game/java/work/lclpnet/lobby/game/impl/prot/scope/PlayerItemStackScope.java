package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerItemStackScope implements Scope<PlayerItemStackScope.Check> {

    @Override
    public Check getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return (player, stack) -> exclude.isWithinScope(player, stack) && !include.isWithinScope(player, stack);
    }

    public interface Check {

        Check CREATIVE_OP = (player, _) -> player.canUseGameMasterBlocks();

        boolean isWithinScope(Player player, ItemStack stack);
    }
}
