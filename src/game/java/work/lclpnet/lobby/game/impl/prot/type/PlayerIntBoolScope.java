package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerIntBoolScope implements Scope<work.lclpnet.lobby.game.api.prot.scope.PlayerIntBoolScope> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerIntBoolScope getGlobalScope() {
        return (player, i, b) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerIntBoolScope getResultingScope(work.lclpnet.lobby.game.api.prot.scope.PlayerIntBoolScope exclude, work.lclpnet.lobby.game.api.prot.scope.PlayerIntBoolScope include) {
        return (player, i, b) -> exclude.isWithinScope(player, i, b) && !include.isWithinScope(player, i, b);
    }
}
