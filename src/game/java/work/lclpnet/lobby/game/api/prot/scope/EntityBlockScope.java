package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface EntityBlockScope {

    EntityBlockScope CREATIVE_OP = (entity, pos) -> entity instanceof ServerPlayerEntity player && player.isCreativeLevelTwoOp();

    boolean isWithinScope(Entity entity, BlockPos pos);
}
