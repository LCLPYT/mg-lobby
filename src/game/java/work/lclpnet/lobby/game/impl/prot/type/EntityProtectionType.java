package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.EntityScope;

public class EntityProtectionType implements ProtectionType<EntityScope> {

    @Override
    public EntityScope getGlobalScope() {
        return entity -> true;
    }

    @Override
    public EntityScope getResultingScope(EntityScope disallowed, EntityScope allowed) {
        return entity -> disallowed.isWithinScope(entity) && !allowed.isWithinScope(entity);
    }
}
