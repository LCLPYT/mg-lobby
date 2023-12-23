package work.lclpnet.lobby.game.api.prot.scope;


import work.lclpnet.kibu.hook.player.PlayerInventoryHooks;

public interface ClickEventScope {

    ClickEventScope CREATIVE_OP = (event) -> event.player().isCreativeLevelTwoOp();

    boolean isWithinScope(PlayerInventoryHooks.ClickEvent event);
}
