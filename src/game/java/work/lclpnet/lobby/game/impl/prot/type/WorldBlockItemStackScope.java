package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class WorldBlockItemStackScope implements Scope<work.lclpnet.lobby.game.api.prot.scope.WorldBlockItemStackScope> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.WorldBlockItemStackScope getGlobalScope() {
        return (world, pos, stack) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.WorldBlockItemStackScope getResultingScope(work.lclpnet.lobby.game.api.prot.scope.WorldBlockItemStackScope exclude, work.lclpnet.lobby.game.api.prot.scope.WorldBlockItemStackScope include) {
        return (world, pos, stack) -> exclude.isWithinScope(world, pos, stack) && !include.isWithinScope(world, pos, stack);
    }
}
