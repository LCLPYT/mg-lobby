package work.lclpnet.lobby.game.impl.prot.scope;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import work.lclpnet.lobby.game.api.prot.Protection;

public class ProjectileHitProtection implements Protection<ProjectileHitProtection.Scope> {

    @Override
    public Scope getGlobalScope() {
        return (_, _) -> true;
    }

    @Override
    public Scope getCombinedExcludeScope(Scope exclude, Scope include) {
        return (projectile, hit) -> exclude.isWithinScope(projectile, hit) && !include.isWithinScope(projectile, hit);
    }

    public interface Scope {

        Scope CREATIVE_OP = (projectile, _) -> projectile.getOwner() instanceof ServerPlayer player
                                                  && player.canUseGameMasterBlocks();

        boolean isWithinScope(Projectile projectile, HitResult hit);
    }
}
