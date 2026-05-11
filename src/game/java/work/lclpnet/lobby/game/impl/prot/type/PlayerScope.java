package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerScope implements Scope<work.lclpnet.lobby.game.api.prot.scope.PlayerScope> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerScope getGlobalScope() {
        return player -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerScope getResultingScope(work.lclpnet.lobby.game.api.prot.scope.PlayerScope exclude, work.lclpnet.lobby.game.api.prot.scope.PlayerScope include) {
        return player -> exclude.isWithinScope(player) && !include.isWithinScope(player);
    }
}
