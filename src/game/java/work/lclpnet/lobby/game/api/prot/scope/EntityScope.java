package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.Entity;

public interface EntityScope {

    boolean isWithinScope(Entity entity);
}
