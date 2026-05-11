package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class EntityDamageSourceScope implements Scope<work.lclpnet.lobby.game.api.prot.scope.EntityDamageSourceScope> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.EntityDamageSourceScope getGlobalScope() {
        return (entity, source) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.EntityDamageSourceScope getResultingScope(work.lclpnet.lobby.game.api.prot.scope.EntityDamageSourceScope exclude, work.lclpnet.lobby.game.api.prot.scope.EntityDamageSourceScope include) {
        return (entity, source) -> exclude.isWithinScope(entity, source) && !include.isWithinScope(entity, source);
    }
}
