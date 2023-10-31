package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.WorldBlockItemStackScope;

public class WorldBlockItemStackProtectionType implements ProtectionType<WorldBlockItemStackScope> {

    @Override
    public WorldBlockItemStackScope getGlobalScope() {
        return (world, pos, stack) -> true;
    }

    @Override
    public WorldBlockItemStackScope getResultingScope(WorldBlockItemStackScope disallowed, WorldBlockItemStackScope allowed) {
        return (world, pos, stack) -> disallowed.isWithinScope(world, pos, stack) && !allowed.isWithinScope(world, pos, stack);
    }
}
