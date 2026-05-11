package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.world.entity.Entity;
import work.lclpnet.lobby.game.api.prot.Scope;

public class PlayerEntityScope<E extends Entity> implements Scope<work.lclpnet.lobby.game.api.prot.scope.PlayerEntityScope<E>> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerEntityScope<E> getGlobalScope() {
        return (player, entity) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.PlayerEntityScope<E> getResultingScope(work.lclpnet.lobby.game.api.prot.scope.PlayerEntityScope<E> exclude, work.lclpnet.lobby.game.api.prot.scope.PlayerEntityScope<E> include) {
        return (player, entity) -> exclude.isWithinScope(player, entity) && !include.isWithinScope(player, entity);
    }
}
