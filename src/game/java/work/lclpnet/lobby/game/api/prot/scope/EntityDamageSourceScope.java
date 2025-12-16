package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;

public interface EntityDamageSourceScope {

    boolean isWithinScope(Entity entity, DamageSource source);
}
