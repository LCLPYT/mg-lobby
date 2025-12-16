package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.HitResult;

public interface ProjectileHitScope {

    ProjectileHitScope CREATIVE_OP = (projectile, hit) -> projectile.getOwner() instanceof ServerPlayer player
                                                          && player.canUseGameMasterBlocks();

    boolean isWithinScope(Projectile projectile, HitResult hit);
}
