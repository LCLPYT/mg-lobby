package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import work.lclpnet.lobby.game.api.prot.Scope;

public class EntityItemEntityScope implements Scope<EntityItemEntityScope.Check> {

    @Override
    public Check getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return (entity, itemEntity) -> exclude.isWithinScope(entity, itemEntity) && !include.isWithinScope(entity, itemEntity);
    }

    public interface Check {

        boolean isWithinScope(Entity entity, ItemEntity itemEntity);
    }
}
