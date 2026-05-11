package work.lclpnet.lobby.game.impl.prot.type;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import work.lclpnet.lobby.game.api.prot.Scope;

public class ProjectileHitScope implements Scope<ProjectileHitScope.Check> {

    @Override
    public Check getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Check getResultingScope(Check exclude, Check include) {
        return (projectile, hit) -> exclude.isWithinScope(projectile, hit) && !include.isWithinScope(projectile, hit);
    }

    public interface Check {

        Check CREATIVE_OP = (projectile, _) -> projectile.getOwner() instanceof ServerPlayer player
                                                  && player.canUseGameMasterBlocks();

        boolean isWithinScope(Projectile projectile, HitResult hit);
    }
}
