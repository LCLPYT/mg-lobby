package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.EntityBlockScope;

public class EntityBlockProtectionType implements ProtectionType<EntityBlockScope> {

    public static final EntityBlockProtectionType INSTANCE = new EntityBlockProtectionType();

    private EntityBlockProtectionType() {}

    @Override
    public EntityBlockScope getGlobalScope() {
        return (entity, pos) -> true;
    }

    @Override
    public EntityBlockScope getResultingScope(EntityBlockScope disallowed, EntityBlockScope allowed) {
        return (entity, pos) -> disallowed.isWithinScope(entity, pos) && !allowed.isWithinScope(entity, pos);
    }
}
