package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.PlayerItemEntityScope;

public class PlayerItemEntityProtectionType implements ProtectionType<PlayerItemEntityScope> {

    @Override
    public PlayerItemEntityScope getGlobalScope() {
        return (player, itemEntity) -> true;
    }

    @Override
    public PlayerItemEntityScope getResultingScope(PlayerItemEntityScope disallowed, PlayerItemEntityScope allowed) {
        return (player, itemEntity) -> disallowed.isWithinScope(player, itemEntity) && !allowed.isWithinScope(player, itemEntity);
    }
}
