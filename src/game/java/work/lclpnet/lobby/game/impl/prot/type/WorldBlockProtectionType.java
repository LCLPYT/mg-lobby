package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.WorldBlockScope;

public class WorldBlockProtectionType implements ProtectionType<WorldBlockScope> {

    public static final WorldBlockProtectionType INSTANCE = new WorldBlockProtectionType();

    private WorldBlockProtectionType() {}

    @Override
    public WorldBlockScope getGlobalScope() {
        return (world, pos) -> true;
    }

    @Override
    public WorldBlockScope getResultingScope(WorldBlockScope disallowed, WorldBlockScope allowed) {
        return (world, pos) -> disallowed.isWithinScope(world, pos) && !allowed.isWithinScope(world, pos);
    }
}
