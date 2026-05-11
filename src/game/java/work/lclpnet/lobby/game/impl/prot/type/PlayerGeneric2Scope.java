package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerGeneric2Scope<T, U> implements Scope<work.lclpnet.lobby.game.api.prot.scope.PlayerGeneric2Scope<T, U>> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerGeneric2Scope<T, U> getGlobalScope() {
        return (player, a, b) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerGeneric2Scope<T, U> getResultingScope(work.lclpnet.lobby.game.api.prot.scope.PlayerGeneric2Scope<T, U> exclude, work.lclpnet.lobby.game.api.prot.scope.PlayerGeneric2Scope<T, U> include) {
        return (player, a, b) -> exclude.isWithinScope(player, a, b) && !include.isWithinScope(player, a, b);
    }
}
