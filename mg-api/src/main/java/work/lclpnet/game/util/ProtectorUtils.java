package work.lclpnet.game.util;

import net.minecraft.server.level.ServerPlayer;
import work.lclpnet.game.impl.prot.MutableProtectionConfig;
import work.lclpnet.game.impl.prot.scope.*;

import java.util.List;

import static work.lclpnet.game.impl.prot.ProtectionTypes.*;

public class ProtectorUtils {

    public static void allowCreativeOperatorBypass(MutableProtectionConfig config) {
        for (EntityBlockProtection scope : List.of(
                BREAK_BLOCKS,
                PLACE_BLOCKS,
                PICKUP_FLUID,
                PICKUP_FLUID,
                CHARGE_RESPAWN_ANCHOR,
                COMPOSTER,
                EAT_CAKE,
                EXPLODE_RESPAWN_LOCATION,
                PRIME_TNT,
                EXTINGUISH_CANDLE,
                TAKE_LECTERN_BOOK,
                EDIT_SIGN,
                USE_BLOCK,
                DECORATED_POT_STORE
        )) {
            scope.allow(config, EntityBlockProtection.Scope.CREATIVE_OP);
        }

        SWAP_HAND_ITEMS.allow(config, PlayerIntProtection.Scope.CREATIVE_OP);
        DROP_ITEM.allow(config, PlayerIntBoolProtection.Scope.CREATIVE_OP);
        PICKUP_ITEM.allow(config, PlayerItemEntityProtection.Scope.CREATIVE_OP);

        for (PlayerEntityProtection<?> scope : List.of(
                ITEM_FRAME_SET_ITEM,
                ITEM_FRAME_REMOVE_ITEM,
                ITEM_FRAME_ROTATE_ITEM
        )) {
            scope.allow(config, PlayerEntityProtection.Scope.creativeOp());
        }

        ARMOR_STAND_MANIPULATE.allow(config, PlayerEntityProtection.Scope.creativeOp());

        // Player, LivingEntity, ItemStack
        USE_ITEM_ON_ENTITY.allow(config, PlayerGeneric2Protection.Scope.creativeOp());

        // Player, ItemUsageContext
        USE_ITEM_ON_BLOCK.allow(config, PlayerGenericProtection.Scope.creativeOp());

        // BlockPos, Collection<Entity>
        LEASH_ENTITIES_TO_BLOCK.allow(config, PlayerGeneric2Protection.Scope.creativeOp());

        // Entity, Collection<Entity>
        LEASH_ENTITIES_TO_ENTITY.allow(config, PlayerGeneric2Protection.Scope.creativeOp());

        // LeashKnotEntity
        LEASH_KNOT_TAKE.allow(config, PlayerEntityProtection.Scope.creativeOp());

        // ProjectileEntity
        PICKUP_PROJECTILE.allow(config, PlayerEntityProtection.Scope.creativeOp());

        // Entity
        for (PlayerEntityProtection<?> scope : List.of(
                MOUNT,
                DESTROY_LEASH,
                ATTACH_LEASH,
                DETACH_LEASH
        )) {
            scope.allow(config, PlayerEntityProtection.Scope.creativeOp());
        }

        ALLOW_DAMAGE.allow(config, (_, source) ->
                source.getEntity() instanceof ServerPlayer player && player.canUseGameMasterBlocks());

        MODIFY_INVENTORY.allow(config, ClickEventProtection.Scope.CREATIVE_OP);

        PROJECTILE_BREAK_DECORATED_POT.allow(config, ProjectileHitProtection.Scope.CREATIVE_OP);

        for (PlayerItemStackProtection scope : List.of(
                CRAFT_ITEM,
                CONSUME_FOOD
        )) {
            scope.allow(config, PlayerItemStackProtection.Scope.CREATIVE_OP);
        }
    }

    private ProtectorUtils() {}
}
