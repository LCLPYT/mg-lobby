package work.lclpnet.lobby.game.api.prot.scope;


import work.lclpnet.kibu.hook.player.PlayerInventoryHooks;

public interface ClickEventScope {

    ClickEventScope CREATIVE_OP = (event) -> event.player().canUseGameMasterBlocks();

    boolean isWithinScope(PlayerInventoryHooks.ClickEvent event);
}
