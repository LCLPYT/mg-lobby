package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerItemEntityScope implements Scope<work.lclpnet.lobby.game.api.prot.scope.PlayerItemEntityScope> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerItemEntityScope getGlobalScope() {
        return (player, itemEntity) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerItemEntityScope getResultingScope(work.lclpnet.lobby.game.api.prot.scope.PlayerItemEntityScope exclude, work.lclpnet.lobby.game.api.prot.scope.PlayerItemEntityScope include) {
        return (player, itemEntity) -> exclude.isWithinScope(player, itemEntity) && !include.isWithinScope(player, itemEntity);
    }
}
