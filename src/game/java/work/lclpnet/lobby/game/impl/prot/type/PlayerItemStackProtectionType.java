package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.PlayerItemStackScope;

public class PlayerItemStackProtectionType implements ProtectionType<PlayerItemStackScope> {

    @Override
    public PlayerItemStackScope getGlobalScope() {
        return (player, stack) -> true;
    }

    @Override
    public PlayerItemStackScope getResultingScope(PlayerItemStackScope disallowed, PlayerItemStackScope allowed) {
        return (player, stack) -> disallowed.isWithinScope(player, stack) && !allowed.isWithinScope(player, stack);
    }
}
