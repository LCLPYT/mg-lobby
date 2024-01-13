package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.EntityDamageSourceScope;

public class EntityDamageSourceProtectionType implements ProtectionType<EntityDamageSourceScope> {

    @Override
    public EntityDamageSourceScope getGlobalScope() {
        return (entity, source) -> true;
    }

    @Override
    public EntityDamageSourceScope getResultingScope(EntityDamageSourceScope disallowed, EntityDamageSourceScope allowed) {
        return (entity, source) -> disallowed.isWithinScope(entity, source) && !allowed.isWithinScope(entity, source);
    }
}
