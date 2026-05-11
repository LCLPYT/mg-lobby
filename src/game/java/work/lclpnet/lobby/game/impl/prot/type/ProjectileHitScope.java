package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.Scope;

public class ProjectileHitScope implements Scope<work.lclpnet.lobby.game.api.prot.scope.ProjectileHitScope> {

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.ProjectileHitScope getGlobalScope() {
        return (projectile, hit) -> true;
    }

    @Override
    public work.lclpnet.lobby.game.api.prot.scope.ProjectileHitScope getResultingScope(work.lclpnet.lobby.game.api.prot.scope.ProjectileHitScope exclude, work.lclpnet.lobby.game.api.prot.scope.ProjectileHitScope include) {
        return (projectile, hit) -> exclude.isWithinScope(projectile, hit) && !include.isWithinScope(projectile, hit);
    }
}
