package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;

public interface EntityDamageSourceScope {

    boolean isWithinScope(Entity entity, DamageSource source);
}
