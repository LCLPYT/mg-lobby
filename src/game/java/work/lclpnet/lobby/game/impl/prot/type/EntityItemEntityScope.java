package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class EntityItemEntityScope implements Scope<work.lclpnet.lobby.game.api.prot.scope.EntityItemEntityScope> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.EntityItemEntityScope getGlobalScope() {
        return (entity, itemEntity) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.EntityItemEntityScope getResultingScope(work.lclpnet.lobby.game.api.prot.scope.EntityItemEntityScope exclude, work.lclpnet.lobby.game.api.prot.scope.EntityItemEntityScope include) {
        return (entity, itemEntity) -> exclude.isWithinScope(entity, itemEntity) && !include.isWithinScope(entity, itemEntity);
    }
}
