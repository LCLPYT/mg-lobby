package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.PlayerIntBoolScope;

public class PlayerIntBoolProtectionType implements ProtectionType<PlayerIntBoolScope> {

    @Override
    public PlayerIntBoolScope getGlobalScope() {
        return (player, i, b) -> true;
    }

    @Override
    public PlayerIntBoolScope getResultingScope(PlayerIntBoolScope disallowed, PlayerIntBoolScope allowed) {
        return (player, i, b) -> disallowed.isWithinScope(player, i, b) && !allowed.isWithinScope(player, i, b);
    }
}
