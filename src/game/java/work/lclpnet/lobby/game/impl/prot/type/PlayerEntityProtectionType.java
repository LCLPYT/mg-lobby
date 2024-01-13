package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.entity.Entity;
import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.PlayerEntityScope;

public class PlayerEntityProtectionType<E extends Entity> implements ProtectionType<PlayerEntityScope<E>> {

    @Override
    public PlayerEntityScope<E> getGlobalScope() {
        return (player, entity) -> true;
    }

    @Override
    public PlayerEntityScope<E> getResultingScope(PlayerEntityScope<E> disallowed, PlayerEntityScope<E> allowed) {
        return (player, entity) -> disallowed.isWithinScope(player, entity) && !allowed.isWithinScope(player, entity);
    }
}
