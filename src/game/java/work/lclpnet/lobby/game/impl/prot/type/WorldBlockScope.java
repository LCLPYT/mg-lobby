package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class WorldBlockScope implements Scope<work.lclpnet.lobby.game.api.prot.scope.WorldBlockScope> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.WorldBlockScope getGlobalScope() {
        return (world, pos) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.WorldBlockScope getResultingScope(work.lclpnet.lobby.game.api.prot.scope.WorldBlockScope exclude, work.lclpnet.lobby.game.api.prot.scope.WorldBlockScope include) {
        return (world, pos) -> exclude.isWithinScope(world, pos) && !include.isWithinScope(world, pos);
    }
}
