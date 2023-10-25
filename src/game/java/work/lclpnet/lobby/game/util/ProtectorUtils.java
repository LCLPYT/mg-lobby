package work.lclpnet.lobby.game.util;

import work.lclpnet.lobby.game.api.prot.scope.EntityBlockScope;
import work.lclpnet.lobby.game.api.prot.scope.PlayerScope;
import work.lclpnet.lobby.game.impl.prot.MutableProtectionConfig;

import static work.lclpnet.lobby.game.impl.prot.ProtectionTypes.*;

public class ProtectorUtils {

    public static void allowCreativeOperatorBypass(MutableProtectionConfig config) {
        config.allow(EntityBlockScope.CREATIVE_OP, BREAK_BLOCKS, PLACE_BLOCKS, USE_ITEM_ON_BLOCK, PICKUP_FLUID,
                PICKUP_FLUID, CHARGE_RESPAWN_ANCHOR, COMPOSTER, EAT_CAKE, EXPLODE_RESPAWN_LOCATION, PRIME_TNT,
                EXTINGUISH_CANDLE, TAKE_LECTERN_BOOK);

        config.allow(DROP_ITEM, PlayerScope.CREATIVE_OP);
    }

    private ProtectorUtils() {}
}
