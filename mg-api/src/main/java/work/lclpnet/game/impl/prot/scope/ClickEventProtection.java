package work.lclpnet.game.impl.prot.scope;

import work.lclpnet.game.api.prot.Protection;
import work.lclpnet.kibu.hook.player.PlayerInventoryHooks;

public class ClickEventProtection implements Protection<ClickEventProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return _ -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return event -> exclude.isWithinScope(event) && !include.isWithinScope(event);
    }

    public interface Scope {

        Scope CREATIVE_OP = (event) -> event.player().canUseGameMasterBlocks();

        boolean isWithinScope(PlayerInventoryHooks.ClickEvent event);
    }
}
