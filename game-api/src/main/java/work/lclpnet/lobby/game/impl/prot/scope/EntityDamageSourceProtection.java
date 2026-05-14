package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import work.lclpnet.lobby.game.api.prot.Protection;

public class EntityDamageSourceProtection implements Protection<EntityDamageSourceProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return (entity, source) -> exclude.isWithinScope(entity, source) && !include.isWithinScope(entity, source);
    }

    public interface Scope {

        boolean isWithinScope(Entity entity, DamageSource source);
    }
}
