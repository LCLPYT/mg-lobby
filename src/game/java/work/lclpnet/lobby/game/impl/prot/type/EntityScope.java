package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class EntityScope implements Scope<work.lclpnet.lobby.game.api.prot.scope.EntityScope> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.EntityScope getGlobalScope() {
        return entity -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.EntityScope getResultingScope(work.lclpnet.lobby.game.api.prot.scope.EntityScope exclude, work.lclpnet.lobby.game.api.prot.scope.EntityScope include) {
        return entity -> exclude.isWithinScope(entity) && !include.isWithinScope(entity);
    }
}
