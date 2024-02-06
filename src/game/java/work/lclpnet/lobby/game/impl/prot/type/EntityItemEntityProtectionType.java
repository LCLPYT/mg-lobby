package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.EntityItemEntityScope;

public class EntityItemEntityProtectionType implements ProtectionType<EntityItemEntityScope> {

    @Override
    public EntityItemEntityScope getGlobalScope() {
        return (entity, itemEntity) -> true;
    }

    @Override
    public EntityItemEntityScope getResultingScope(EntityItemEntityScope disallowed, EntityItemEntityScope allowed) {
        return (entity, itemEntity) -> disallowed.isWithinScope(entity, itemEntity) && !allowed.isWithinScope(entity, itemEntity);
    }
}
