package work.lclpnet.lobby.game.impl.prot;

import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import work.lclpnet.kibu.hook.Hook;
import work.lclpnet.kibu.hook.entity.*;
import work.lclpnet.kibu.hook.player.PlayerFoodHooks;
import work.lclpnet.kibu.hook.player.PlayerInventoryHooks;
import work.lclpnet.kibu.hook.world.BlockModificationHooks;
import work.lclpnet.kibu.hook.world.WorldPhysicsHooks;
import work.lclpnet.kibu.plugin.hook.HookContainer;
import work.lclpnet.lobby.game.api.prot.ProtectionConfig;
import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.Protector;
import work.lclpnet.lobby.game.api.prot.scope.EntityBlockScope;
import work.lclpnet.mplugins.ext.Unloadable;

import java.util.function.Consumer;
import java.util.function.Function;

import static work.lclpnet.lobby.game.impl.prot.ProtectionTypes.*;

public class BasicProtector implements Protector, Unloadable {

    private final ProtectionConfig config;
    private final HookContainer hooks;

    public BasicProtector(ProtectionConfig config) {
        this.config = config;
        this.hooks = new HookContainer();
    }

    public void activate() {
        protect(BREAK_BLOCKS, BlockModificationHooks.BREAK_BLOCK, BasicProtector::onModify);

        protect(PLACE_BLOCKS, BlockModificationHooks.PLACE_BLOCK, scope
                -> (world, pos, entity, newState)
                -> scope.isWithinScope(entity, pos));

        protect(PICKUP_FLUID, BlockModificationHooks.PICKUP_FLUID, scope
                -> (world, pos, entity, fluid)
                -> scope.isWithinScope(entity, pos));

        protect(PLACE_FLUID, BlockModificationHooks.PLACE_FLUID, scope
                -> (world, pos, entity, fluid)
                -> scope.isWithinScope(entity, pos));

        protect(USE_ITEM_ON_BLOCK, BlockModificationHooks.USE_ITEM_ON_BLOCK, scope
                -> (ctx)
                -> scope.isWithinScope(ctx.getPlayer(), ctx.getBlockPos()) ? ActionResult.FAIL : null);

        protect(TRAMPLE_FARMLAND, BlockModificationHooks.TRAMPLE_FARMLAND, BasicProtector::onModify);

        protect(HUNGER, scope -> {
            hooks.registerHook(PlayerFoodHooks.LEVEL_CHANGE, (player, fromLevel, toLevel)
                    -> fromLevel > toLevel && scope.isWithinScope(player));
            hooks.registerHook(PlayerFoodHooks.EXHAUSTION_CHANGE, (player, fromLevel, toLevel)
                    -> fromLevel > toLevel && scope.isWithinScope(player));
            hooks.registerHook(PlayerFoodHooks.SATURATION_CHANGE, (player, fromLevel, toLevel)
                    -> fromLevel > toLevel && scope.isWithinScope(player));
        });

        protect(MOB_GRIEFING, BlockModificationHooks.CAN_MOB_GRIEF, scope
                -> (world, pos, entity)
                -> scope.isWithinScope(entity, pos));

        protect(CHARGE_RESPAWN_ANCHOR, BlockModificationHooks.CHARGE_RESPAWN_ANCHOR, BasicProtector::onModify);

        protect(COMPOSTER, BlockModificationHooks.COMPOSTER, BasicProtector::onModify);

        protect(EAT_CAKE, BlockModificationHooks.EAT_CAKE, BasicProtector::onModify);

        protect(EXPLODE_RESPAWN_LOCATION, BlockModificationHooks.EXPLODE_RESPAWN_LOCATION, BasicProtector::onModify);

        protect(EXTINGUISH_CANDLE, BlockModificationHooks.EXTINGUISH_CANDLE, BasicProtector::onModify);

        protect(PRIME_TNT, BlockModificationHooks.PRIME_TNT, BasicProtector::onModify);

        protect(TAKE_LECTERN_BOOK, BlockModificationHooks.TAKE_LECTERN_BOOK, BasicProtector::onModify);

        protect(TRAMPLE_TURTLE_EGG, BlockModificationHooks.TRAMPLE_TURTLE_EGG, BasicProtector::onModify);

        protect(CAULDRON_DRIP_STONE, WorldPhysicsHooks.CAULDRON_DRIP_STONE, scope
                -> (world, pos, newState)
                -> scope.isWithinScope(world, pos));

        protect(EXPLOSION, WorldPhysicsHooks.EXPLOSION, scope
                -> (exploder)
                -> scope.isWithinScope(exploder, exploder.getBlockPos()));

        protect(MELT, WorldPhysicsHooks.MELT, scope -> scope::isWithinScope);

        protect(FREEZE, WorldPhysicsHooks.FREEZE, scope -> scope::isWithinScope);

        protect(SNOW_FALL, WorldPhysicsHooks.SNOW_FALL, scope -> scope::isWithinScope);

        protect(CAULDRON_PRECIPITATION, WorldPhysicsHooks.CAULDRON_PRECIPITATION, scope
                -> (world, pos, newState)
                -> scope.isWithinScope(world, pos));

        protect(FROST_WALKER_FREEZE, WorldPhysicsHooks.FROST_WALKER_FREEZE, scope
                -> (world, pos, entity)
                -> scope.isWithinScope(entity, pos));

        protect(DROP_ITEM, PlayerInventoryHooks.DROP_ITEM, scope
                -> scope::isWithinScope);

        protect(ALLOW_DAMAGE, scope -> {
            // allow damage - true means allow
            hooks.registerHook(ServerLivingEntityHooks.ALLOW_DAMAGE, (entity, source, amount)
                    -> source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || !scope.isWithinScope(entity, source));

            // non-living damage - true means cancel (no damage)
            hooks.registerHook(NonLivingDamageCallback.HOOK, (entity, source, amount)
                    -> !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && scope.isWithinScope(entity, source));
        });

        protect(ALLOW_DAMAGE, ServerLivingEntityHooks.ALLOW_DAMAGE, scope
                -> (entity, source, amount)
                -> source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || !scope.isWithinScope(entity, source));  // allow damage is inverted

        protect(PICKUP_ITEM, PlayerInventoryHooks.PLAYER_PICKUP, scope
                -> scope::isWithinScope);

        protect(BLOCK_ITEM_DROP, WorldPhysicsHooks.BLOCK_ITEM_DROP, scope
                -> scope::isWithinScope);

        protect(BLOCK_XP_DROP, WorldPhysicsHooks.BLOCK_XP_DROP, scope
                -> (world, pos, xp)
                -> scope.isWithinScope(world, pos));

        protect(SWAP_HAND_ITEMS, PlayerInventoryHooks.SWAP_HANDS, scope
                -> scope::isWithinScope);

        protect(ITEM_FRAME_SET_ITEM, ItemFramePutItemCallback.HOOK, scope
                -> (itemFrame, stack, player, hand)
                -> scope.isWithinScope(player, itemFrame));

        protect(ITEM_FRAME_REMOVE_ITEM, ItemFrameRemoveItemCallback.HOOK, scope
                -> (itemFrame, attacker)
                -> attacker instanceof ServerPlayerEntity player && scope.isWithinScope(player, itemFrame));

        protect(ITEM_FRAME_ROTATE_ITEM, ItemFrameRotateCallback.HOOK, scope
                -> (itemFrame, player, hand)
                -> scope.isWithinScope(player, itemFrame));

        protect(ARMOR_STAND_MANIPULATE, ArmorStandManipulateCallback.HOOK, scope
                -> (armorStand, player, slot, stack, hand)
                -> scope.isWithinScope(player, armorStand));

        protect(USE_ITEM_ON_ENTITY, ItemUseOnEntityCallback.HOOK, scope
                -> (player, entity, hand, stack)
                -> scope.isWithinScope(player, entity));

        protect(ATTACH_LEASH, LeashAttachCallback.HOOK, scope
                -> (player, world, pos)
                -> scope.isWithinScope(player, pos));

        protect(DETACH_LEASH, LeashDetachCallback.HOOK, scope
                -> scope::isWithinScope);

        protect(LEASH_MOB, LeashEntityCallback.HOOK, scope
                -> scope::isWithinScope);

        protect(UNLEASH_MOB, UnleashEntityCallback.HOOK, scope
                -> scope::isWithinScope);

        protect(LEASH_MOB_TO_BLOCK, LeashEntityToBlockCallback.HOOK, scope
                -> (player, entity, leashKnot)
                -> scope.isWithinScope(player, entity));

        protect(PICKUP_PROJECTILE, ProjectilePickupCallback.HOOK, scope
                -> scope::isWithinScope);

        protect(MODIFY_INVENTORY, PlayerInventoryHooks.MODIFY_INVENTORY, scope
                -> scope::isWithinScope);
    }

    @Override
    public void deactivate() {
        hooks.unload();
    }

    @Override
    public void unload() {
        deactivate();
    }

    private static BlockModificationHooks.BlockModifyHook onModify(EntityBlockScope scope) {
        return (world, pos, entity) -> scope.isWithinScope(entity, pos);
    }

    private <T> void protect(ProtectionType<T> type, Consumer<T> setupAction) {
        withScope(type, setupAction);
    }

    private <T, H> void protect(ProtectionType<T> type, Hook<H> hook, Function<T, H> listenerFactory) {
        withScope(type, scope -> {
            H listener = listenerFactory.apply(scope);
            hooks.registerHook(hook, listener);
        });
    }

    private <T> void withScope(ProtectionType<T> type, Consumer<T> action) {
        if (!config.hasRestrictions(type)) return;

        T disallowed = config.getDisallowedScope(type);

        if (disallowed == null) return;

        T allowed = config.getAllowedScope(type);

        if (allowed == null) {
            action.accept(disallowed);
            return;
        }

        // get the resulting scope that is the difference between disallowed and allowed
        T resultingScope = type.getResultingScope(disallowed, allowed);
        action.accept(resultingScope);
    }
}
