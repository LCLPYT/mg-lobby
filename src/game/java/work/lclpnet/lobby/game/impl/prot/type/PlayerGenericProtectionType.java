package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.PlayerGenericScope;

public class PlayerGenericProtectionType<T> implements ProtectionType<PlayerGenericScope<T>> {

    @Override
    public PlayerGenericScope<T> getGlobalScope() {
        return (player, entity) -> true;
    }

    @Override
    public PlayerGenericScope<T> getResultingScope(PlayerGenericScope<T> disallowed, PlayerGenericScope<T> allowed) {
        return (player, entity) -> disallowed.isWithinScope(player, entity) && !allowed.isWithinScope(player, entity);
    }
}
