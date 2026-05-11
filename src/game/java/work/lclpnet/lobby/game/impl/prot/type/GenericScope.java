package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class GenericScope<T> implements Scope<work.lclpnet.lobby.game.api.prot.scope.GenericScope<T>> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.GenericScope<T> getGlobalScope() {
        return arg -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.GenericScope<T> getResultingScope(work.lclpnet.lobby.game.api.prot.scope.GenericScope<T> exclude, work.lclpnet.lobby.game.api.prot.scope.GenericScope<T> include) {
        return arg -> exclude.isWithinScope(arg) && !include.isWithinScope(arg);
    }
}
