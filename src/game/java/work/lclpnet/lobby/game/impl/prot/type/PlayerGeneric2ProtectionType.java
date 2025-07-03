package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.PlayerGeneric2Scope;

public class PlayerGeneric2ProtectionType<T, U> implements ProtectionType<PlayerGeneric2Scope<T, U>> {

    @Override
    public PlayerGeneric2Scope<T, U> getGlobalScope() {
        return (player, a, b) -> true;
    }

    @Override
    public PlayerGeneric2Scope<T, U> getResultingScope(PlayerGeneric2Scope<T, U> disallowed, PlayerGeneric2Scope<T, U> allowed) {
        return (player, a, b) -> disallowed.isWithinScope(player, a, b) && !allowed.isWithinScope(player, a, b);
    }
}
