package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.kibu.hook.player.PlayerInventoryHooks;
import work.lclpnet.lobby.game.api.prot.Scope;

public class ClickEventScope implements Scope<ClickEventScope.Check> {

    @Override
    public Check getGlobalScope() {
        return _ -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return event -> exclude.isWithinScope(event) && !include.isWithinScope(event);
    }

    public interface Check {

        Check CREATIVE_OP = (event) -> event.player().canUseGameMasterBlocks();

        boolean isWithinScope(PlayerInventoryHooks.ClickEvent event);
    }
}
