package work.lclpnet.lobby.game.util;

import net.minecraft.server.level.ServerPlayer;
import work.lclpnet.lobby.game.impl.prot.scope.*;
import work.lclpnet.lobby.game.impl.prot.MutableProtectionConfig;

import java.util.List;

import static work.lclpnet.lobby.game.impl.prot.ProtectionTypes.*;

public class ProtectorUtils {

    public static void allowCreativeOperatorBypass(MutableProtectionConfig config) {
        for (EntityBlockScope scope : List.of(
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
            scope.allow(config, EntityBlockScope.Check.CREATIVE_OP);
        }

        SWAP_HAND_ITEMS.allow(config, PlayerIntScope.Check.CREATIVE_OP);
        DROP_ITEM.allow(config, PlayerIntBoolScope.Check.CREATIVE_OP);
        PICKUP_ITEM.allow(config, PlayerItemEntityScope.Check.CREATIVE_OP);

        for (PlayerEntityScope<?> scope : List.of(
                ITEM_FRAME_SET_ITEM,
                ITEM_FRAME_REMOVE_ITEM,
                ITEM_FRAME_ROTATE_ITEM
        )) {
            scope.allow(config, PlayerEntityScope.Check.creativeOp());
        }

        ARMOR_STAND_MANIPULATE.allow(config, PlayerEntityScope.Check.creativeOp());

        // Player, LivingEntity, ItemStack
        USE_ITEM_ON_ENTITY.allow(config, PlayerGeneric2Scope.Check.creativeOp());

        // Player, ItemUsageContext
        USE_ITEM_ON_BLOCK.allow(config, PlayerGenericScope.Check.creativeOp());

        // BlockPos, Collection<Entity>
        LEASH_ENTITIES_TO_BLOCK.allow(config, PlayerGeneric2Scope.Check.creativeOp());

        // Entity, Collection<Entity>
        LEASH_ENTITIES_TO_ENTITY.allow(config, PlayerGeneric2Scope.Check.creativeOp());

        // LeashKnotEntity
        LEASH_KNOT_TAKE.allow(config, PlayerEntityScope.Check.creativeOp());

        // ProjectileEntity
        PICKUP_PROJECTILE.allow(config, PlayerEntityScope.Check.creativeOp());

        // Entity
        for (PlayerEntityScope<?> scope : List.of(
                MOUNT,
                DESTROY_LEASH,
                ATTACH_LEASH,
                DETACH_LEASH
        )) {
            scope.allow(config, PlayerEntityScope.Check.creativeOp());
        }

        ALLOW_DAMAGE.allow(config, (_, source) ->
                source.getEntity() instanceof ServerPlayer player && player.canUseGameMasterBlocks());

        MODIFY_INVENTORY.allow(config, ClickEventScope.Check.CREATIVE_OP);

        PROJECTILE_BREAK_DECORATED_POT.allow(config, ProjectileHitScope.Check.CREATIVE_OP);

        for (PlayerItemStackScope scope : List.of(
                CRAFT_ITEM,
                CONSUME_FOOD
        )) {
            scope.allow(config, PlayerItemStackScope.Check.CREATIVE_OP);
        }
    }

    private ProtectorUtils() {}
}
