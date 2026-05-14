package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import work.lclpnet.lobby.game.api.prot.Protection;

public class EntityItemEntityProtection implements Protection<EntityItemEntityProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return (entity, itemEntity) -> exclude.isWithinScope(entity, itemEntity) && !include.isWithinScope(entity, itemEntity);
    }

    public interface Scope {

        boolean isWithinScope(Entity entity, ItemEntity itemEntity);
    }
}
