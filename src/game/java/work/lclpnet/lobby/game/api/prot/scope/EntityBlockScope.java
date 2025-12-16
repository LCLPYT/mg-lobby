package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;

public interface EntityBlockScope {

    EntityBlockScope CREATIVE_OP = (entity, pos) -> entity instanceof ServerPlayer player && player.canUseGameMasterBlocks();

    boolean isWithinScope(Entity entity, BlockPos pos);
}
