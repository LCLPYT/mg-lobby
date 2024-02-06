package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.ItemScatterScope;

public class ItemScatterProtectionType implements ProtectionType<ItemScatterScope> {

    @Override
    public ItemScatterScope getGlobalScope() {
        return (world, x, y, z, stack) -> true;
    }

    @Override
    public ItemScatterScope getResultingScope(ItemScatterScope disallowed, ItemScatterScope allowed) {
        return (world, x, y, z, stack) -> disallowed.isWithinScope(world, x, y, z, stack) && !allowed.isWithinScope(world, x, y, z, stack);
    }
}
