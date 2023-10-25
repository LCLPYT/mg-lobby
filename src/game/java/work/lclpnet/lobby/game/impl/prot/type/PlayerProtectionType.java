package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.PlayerScope;

public class PlayerProtectionType implements ProtectionType<PlayerScope> {

    @Override
    public PlayerScope getGlobalScope() {
        return player -> true;
    }

    @Override
    public PlayerScope getResultingScope(PlayerScope disallowed, PlayerScope allowed) {
        return player -> disallowed.isWithinScope(player) && !allowed.isWithinScope(player);
    }
}
