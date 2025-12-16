package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

public interface EntityItemEntityScope {

    boolean isWithinScope(Entity entity, ItemEntity itemEntity);
}
