package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import work.lclpnet.lobby.game.api.prot.Scope;

public class EntityBlockScope implements Scope<EntityBlockScope.Check> {

    @Override
    public Check getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return (entity, pos) -> exclude.isWithinScope(entity, pos) && !include.isWithinScope(entity, pos);
    }

    public interface Check {

        Check CREATIVE_OP = (entity, _) -> entity instanceof ServerPlayer player && player.canUseGameMasterBlocks();

        boolean isWithinScope(Entity entity, BlockPos pos);
    }
}
