package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.entity.Entity;
import work.lclpnet.lobby.game.api.prot.Protection;

public class EntityProtection implements Protection<EntityProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return _ -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return entity -> exclude.isWithinScope(entity) && !include.isWithinScope(entity);
    }

    public interface Scope {

        boolean isWithinScope(Entity entity);
    }
}
