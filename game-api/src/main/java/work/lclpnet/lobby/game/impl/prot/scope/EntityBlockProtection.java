package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import work.lclpnet.lobby.game.api.prot.Protection;

public class EntityBlockProtection implements Protection<EntityBlockProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return (entity, pos) -> exclude.isWithinScope(entity, pos) && !include.isWithinScope(entity, pos);
    }

    public interface Scope {

        Scope CREATIVE_OP = (entity, _) -> entity instanceof ServerPlayer player && player.canUseGameMasterBlocks();

        boolean isWithinScope(Entity entity, BlockPos pos);
    }
}
