package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerItemStackScope implements Scope<work.lclpnet.lobby.game.api.prot.scope.PlayerItemStackScope> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerItemStackScope getGlobalScope() {
        return (player, stack) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerItemStackScope getResultingScope(work.lclpnet.lobby.game.api.prot.scope.PlayerItemStackScope exclude, work.lclpnet.lobby.game.api.prot.scope.PlayerItemStackScope include) {
        return (player, stack) -> exclude.isWithinScope(player, stack) && !include.isWithinScope(player, stack);
    }
}
