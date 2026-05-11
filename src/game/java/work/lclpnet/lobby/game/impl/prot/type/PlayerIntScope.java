package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerIntScope implements Scope<work.lclpnet.lobby.game.api.prot.scope.PlayerIntScope> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerIntScope getGlobalScope() {
        return (player, i) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerIntScope getResultingScope(work.lclpnet.lobby.game.api.prot.scope.PlayerIntScope exclude, work.lclpnet.lobby.game.api.prot.scope.PlayerIntScope include) {
        return (player, i) -> exclude.isWithinScope(player, i) && !include.isWithinScope(player, i);
    }
}
