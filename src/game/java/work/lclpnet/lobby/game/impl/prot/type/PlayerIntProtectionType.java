package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.PlayerIntScope;

public class PlayerIntProtectionType implements ProtectionType<PlayerIntScope> {

    @Override
    public PlayerIntScope getGlobalScope() {
        return (player, i) -> true;
    }

    @Override
    public PlayerIntScope getResultingScope(PlayerIntScope disallowed, PlayerIntScope allowed) {
        return (player, i) -> disallowed.isWithinScope(player, i) && !allowed.isWithinScope(player, i);
    }
}
