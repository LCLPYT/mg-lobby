package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.world.entity.Entity;
import work.lclpnet.lobby.game.api.prot.Scope;

public class EntityScope implements Scope<EntityScope.Check> {

    @Override
    public Check getGlobalScope() {
        return _ -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return entity -> exclude.isWithinScope(entity) && !include.isWithinScope(entity);
    }

    public interface Check {

        boolean isWithinScope(Entity entity);
    }
}
