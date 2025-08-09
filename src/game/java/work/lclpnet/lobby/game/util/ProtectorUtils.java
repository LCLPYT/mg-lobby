package work.lclpnet.lobby.game.util;

import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.lobby.game.api.prot.scope.*;
import work.lclpnet.lobby.game.impl.prot.MutableProtectionConfig;

import static work.lclpnet.lobby.game.impl.prot.ProtectionTypes.*;

public class ProtectorUtils {

    public static void allowCreativeOperatorBypass(MutableProtectionConfig config) {
        config.allow(EntityBlockScope.CREATIVE_OP, BREAK_BLOCKS, PLACE_BLOCKS, PICKUP_FLUID,
                PICKUP_FLUID, CHARGE_RESPAWN_ANCHOR, COMPOSTER, EAT_CAKE, EXPLODE_RESPAWN_LOCATION, PRIME_TNT,
                EXTINGUISH_CANDLE, TAKE_LECTERN_BOOK, EDIT_SIGN, USE_BLOCK, DECORATED_POT_STORE);

        config.allow(SWAP_HAND_ITEMS, PlayerIntScope.CREATIVE_OP);
        config.allow(DROP_ITEM, PlayerIntBoolScope.CREATIVE_OP);
        config.allow(PICKUP_ITEM, PlayerItemEntityScope.CREATIVE_OP);

        config.allow(PlayerEntityScope.creativeOp(), ITEM_FRAME_SET_ITEM, ITEM_FRAME_REMOVE_ITEM,
                ITEM_FRAME_ROTATE_ITEM);

        config.allow(PlayerEntityScope.creativeOp(), ARMOR_STAND_MANIPULATE);

        // Player, LivingEntity, ItemStack
        config.allow(PlayerGeneric2Scope.creativeOp(), USE_ITEM_ON_ENTITY);

        // Player, ItemUsageContext
        config.allow(PlayerGenericScope.creativeOp(), USE_ITEM_ON_BLOCK);

        // BlockPos, Collection<Entity>
        config.allow(PlayerGeneric2Scope.creativeOp(), LEASH_ENTITIES_TO_BLOCK);

        // Entity, Collection<Entity>
        config.allow(PlayerGeneric2Scope.creativeOp(), LEASH_ENTITIES_TO_ENTITY);

        // LeashKnotEntity
        config.allow(PlayerEntityScope.creativeOp(), LEASH_KNOT_TAKE);

        // ProjectileEntity
        config.allow(PlayerEntityScope.creativeOp(), PICKUP_PROJECTILE);

        // Entity
        config.allow(PlayerEntityScope.creativeOp(), MOUNT, DESTROY_LEASH, ATTACH_LEASH, DETACH_LEASH);

        config.allow(ALLOW_DAMAGE, (entity, source) -> source.getAttacker() instanceof ServerPlayerEntity player
                                                       && player.isCreativeLevelTwoOp());

        config.allow(MODIFY_INVENTORY, ClickEventScope.CREATIVE_OP);

        config.allow(PROJECTILE_BREAK_DECORATED_POT, ProjectileHitScope.CREATIVE_OP);

        config.allow(PlayerItemStackScope.CREATIVE_OP, CRAFT_ITEM, CONSUME_FOOD);
    }

    private ProtectorUtils() {}
}
