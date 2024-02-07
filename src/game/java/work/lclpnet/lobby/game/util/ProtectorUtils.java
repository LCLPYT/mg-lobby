package work.lclpnet.lobby.game.util;

import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.lobby.game.api.prot.scope.*;
import work.lclpnet.lobby.game.impl.prot.MutableProtectionConfig;

import static work.lclpnet.lobby.game.impl.prot.ProtectionTypes.*;

public class ProtectorUtils {

    public static void allowCreativeOperatorBypass(MutableProtectionConfig config) {
        config.allow(EntityBlockScope.CREATIVE_OP, BREAK_BLOCKS, PLACE_BLOCKS, USE_ITEM_ON_BLOCK, PICKUP_FLUID,
                PICKUP_FLUID, CHARGE_RESPAWN_ANCHOR, COMPOSTER, EAT_CAKE, EXPLODE_RESPAWN_LOCATION, PRIME_TNT,
                EXTINGUISH_CANDLE, TAKE_LECTERN_BOOK, ATTACH_LEASH, EDIT_SIGN, USE_BLOCK, DECORATED_POT_STORE);

        config.allow(SWAP_HAND_ITEMS, PlayerIntScope.CREATIVE_OP);
        config.allow(DROP_ITEM, PlayerIntBoolScope.CREATIVE_OP);
        config.allow(PICKUP_ITEM, PlayerItemEntityScope.CREATIVE_OP);

        config.allow(PlayerEntityScope.creativeOp(), ITEM_FRAME_SET_ITEM, ITEM_FRAME_REMOVE_ITEM,
                ITEM_FRAME_ROTATE_ITEM);

        config.allow(PlayerEntityScope.creativeOp(), ARMOR_STAND_MANIPULATE);

        // LivingEntity
        config.allow(PlayerEntityScope.creativeOp(), USE_ITEM_ON_ENTITY);

        // MobEntity
        config.allow(PlayerEntityScope.creativeOp(), LEASH_MOB, UNLEASH_MOB, LEASH_MOB_TO_BLOCK);

        config.allow(PlayerEntityScope.creativeOp(), DETACH_LEASH);

        config.allow(PlayerEntityScope.creativeOp(), PICKUP_PROJECTILE);

        config.allow(ALLOW_DAMAGE, (entity, source) -> source.getAttacker() instanceof ServerPlayerEntity player
                                                       && player.isCreativeLevelTwoOp());

        config.allow(MODIFY_INVENTORY, ClickEventScope.CREATIVE_OP);

        config.allow(PROJECTILE_BREAK_DECORATED_POT, ProjectileHitScope.CREATIVE_OP);

        config.allow(CRAFT_ITEM, PlayerItemStackScope.CREATIVE_OP);
    }

    private ProtectorUtils() {}
}
