package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class ItemScatterScope implements Scope<work.lclpnet.lobby.game.api.prot.scope.ItemScatterScope> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.ItemScatterScope getGlobalScope() {
        return (world, x, y, z, stack) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.ItemScatterScope getResultingScope(work.lclpnet.lobby.game.api.prot.scope.ItemScatterScope exclude, work.lclpnet.lobby.game.api.prot.scope.ItemScatterScope include) {
        return (world, x, y, z, stack) -> exclude.isWithinScope(world, x, y, z, stack) && !include.isWithinScope(world, x, y, z, stack);
    }
}
