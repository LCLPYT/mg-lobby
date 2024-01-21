package work.lclpnet.lobby.game.api.prot.scope;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;

public interface ProjectileHitScope {

    ProjectileHitScope CREATIVE_OP = (projectile, hit) -> projectile.getOwner() instanceof ServerPlayerEntity player
                                                          && player.isCreativeLevelTwoOp();

    boolean isWithinScope(ProjectileEntity projectile, HitResult hit);
}
