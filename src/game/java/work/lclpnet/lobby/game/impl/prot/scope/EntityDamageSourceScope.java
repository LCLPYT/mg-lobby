package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import work.lclpnet.lobby.game.api.prot.Scope;

public class EntityDamageSourceScope implements Scope<EntityDamageSourceScope.Check> {

    @Override
    public Check getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return (entity, source) -> exclude.isWithinScope(entity, source) && !include.isWithinScope(entity, source);
    }

    public interface Check {

        boolean isWithinScope(Entity entity, DamageSource source);
    }
}
