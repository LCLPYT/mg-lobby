package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.ProjectileHitScope;

public class ProjectileHitProtectionType implements ProtectionType<ProjectileHitScope> {

    @Override
    public ProjectileHitScope getGlobalScope() {
        return (projectile, hit) -> true;
    }

    @Override
    public ProjectileHitScope getResultingScope(ProjectileHitScope disallowed, ProjectileHitScope allowed) {
        return (projectile, hit) -> disallowed.isWithinScope(projectile, hit) && !allowed.isWithinScope(projectile, hit);
    }
}
