package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;

public interface EntityItemEntityScope {

    boolean isWithinScope(Entity entity, ItemEntity itemEntity);
}
