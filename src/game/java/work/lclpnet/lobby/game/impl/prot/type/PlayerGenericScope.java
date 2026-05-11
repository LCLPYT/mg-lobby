package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerGenericScope<T> implements Scope<work.lclpnet.lobby.game.api.prot.scope.PlayerGenericScope<T>> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerGenericScope<T> getGlobalScope() {
        return (player, entity) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerGenericScope<T> getResultingScope(work.lclpnet.lobby.game.api.prot.scope.PlayerGenericScope<T> exclude, work.lclpnet.lobby.game.api.prot.scope.PlayerGenericScope<T> include) {
        return (player, entity) -> exclude.isWithinScope(player, entity) && !include.isWithinScope(player, entity);
    }
}
