package work.lclpnet.game.impl.prot;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import work.lclpnet.kibu.hook.Hook;
import work.lclpnet.kibu.hook.HookContainer;
import work.lclpnet.kibu.hook.entity.*;
import work.lclpnet.kibu.hook.entity.leash.*;
import work.lclpnet.kibu.hook.level.BlockModificationHooks;
import work.lclpnet.kibu.hook.level.ItemScatterCallback;
import work.lclpnet.kibu.hook.level.LevelPhysicsHooks;
import work.lclpnet.kibu.hook.player.CraftingRecipeCallback;
import work.lclpnet.kibu.hook.player.PlayerFoodHooks;
import work.lclpnet.kibu.hook.player.PlayerInventoryHooks;
import work.lclpnet.kibu.hook.util.PendingResult;
import work.lclpnet.kibu.hook.util.PlayerUtils;
import work.lclpnet.game.api.prot.ProtectionConfig;
import work.lclpnet.game.api.prot.Protector;
import work.lclpnet.game.api.prot.Protection;
import work.lclpnet.game.impl.prot.scope.EntityBlockProtection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static work.lclpnet.game.impl.prot.ProtectionTypes.*;

public class BasicProtector implements Protector {

    private final ProtectionConfig config;
    private final HookContainer hooks;
    private final Set<Block> functionalBlocks;

    public BasicProtector(ProtectionConfig config) {
        this.config = config;
        this.hooks = new HookContainer();
        this.functionalBlocks = collectFunctionalBlocks();
    }

    public void activate() {
        protect(BREAK_BLOCKS, BlockModificationHooks.BREAK_BLOCK, BasicProtector::onModify);

        protect(PLACE_BLOCKS, BlockModificationHooks.PLACE_BLOCK, scope
                -> (_, pos, entity, _)
                -> scope.isWithinScope(entity, pos));

        protect(PICKUP_FLUID, BlockModificationHooks.PICKUP_FLUID, scope
                -> (_, pos, entity, _)
                -> scope.isWithinScope(entity, pos));

        protect(PLACE_FLUID, BlockModificationHooks.PLACE_FLUID, scope
                -> (_, pos, entity, _)
                -> scope.isWithinScope(entity, pos));

        protect(USE_ITEM_ON_BLOCK, BlockModificationHooks.USE_ITEM_ON_BLOCK, scope
                -> (ctx)
                -> scope.isWithinScope(ctx.getPlayer(), ctx) ? InteractionResult.FAIL : null);

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
                -> (_, pos, entity)
                -> scope.isWithinScope(entity, pos));

        protect(CHARGE_RESPAWN_ANCHOR, BlockModificationHooks.CHARGE_RESPAWN_ANCHOR, BasicProtector::onModify);

        protect(COMPOSTER, BlockModificationHooks.COMPOSTER, BasicProtector::onModify);

        protect(EAT_CAKE, BlockModificationHooks.EAT_CAKE, BasicProtector::onModify);

        protect(EXPLODE_RESPAWN_LOCATION, BlockModificationHooks.EXPLODE_RESPAWN_LOCATION, BasicProtector::onModify);

        protect(EXTINGUISH_CANDLE, BlockModificationHooks.EXTINGUISH_CANDLE, BasicProtector::onModify);

        protect(PRIME_TNT, BlockModificationHooks.PRIME_TNT, BasicProtector::onModify);

        protect(TAKE_LECTERN_BOOK, BlockModificationHooks.TAKE_LECTERN_BOOK, BasicProtector::onModify);

        protect(TRAMPLE_TURTLE_EGG, BlockModificationHooks.TRAMPLE_TURTLE_EGG, BasicProtector::onModify);

        protect(CAULDRON_DRIP_STONE, LevelPhysicsHooks.CAULDRON_DRIP_STONE, scope
                -> (world, pos, _)
                -> scope.isWithinScope(world, pos));

        protect(EXPLOSION, LevelPhysicsHooks.EXPLOSION, scope -> scope::isWithinScope);

        protect(MELT, LevelPhysicsHooks.MELT, scope -> scope::isWithinScope);

        protect(FREEZE, LevelPhysicsHooks.FREEZE, scope -> scope::isWithinScope);

        protect(SNOW_FALL, LevelPhysicsHooks.SNOW_FALL, scope -> scope::isWithinScope);

        protect(CAULDRON_PRECIPITATION, LevelPhysicsHooks.CAULDRON_PRECIPITATION, scope
                -> (world, pos, _)
                -> scope.isWithinScope(world, pos));

        protect(REPLACE_DISK_ENCHANTMENT, LevelPhysicsHooks.REPLACE_DISK_ENCHANTMENT, scope
                -> (world, pos, entity, _)
                -> scope.isWithinScope(entity, pos));

        protect(DROP_ITEM, PlayerInventoryHooks.DROP_ITEM, scope
                -> scope::isWithinScope);

        protect(ALLOW_DAMAGE, scope -> {
            // allow damage - true means allow
            hooks.registerHook(ServerLivingEntityHooks.ALLOW_DAMAGE, (entity, source, amount)
                    -> source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || !scope.isWithinScope(entity, source));

            // non-living damage - true means cancel (no damage)
            hooks.registerHook(NonLivingDamageCallback.HOOK, (entity, source, amount)
                    -> !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && scope.isWithinScope(entity, source));
        });

        protect(ALLOW_DAMAGE, ServerLivingEntityHooks.ALLOW_DAMAGE, scope
                -> (entity, source, amount)
                -> source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || !scope.isWithinScope(entity, source));  // allow damage is inverted

        protect(PICKUP_ITEM, PlayerInventoryHooks.PLAYER_PICKUP, scope
                -> scope::isWithinScope);

        protect(BLOCK_ITEM_DROP, LevelPhysicsHooks.BLOCK_ITEM_DROP, scope
                -> scope::isWithinScope);

        protect(BLOCK_XP_DROP, LevelPhysicsHooks.BLOCK_XP_DROP, scope
                -> (world, pos, _)
                -> scope.isWithinScope(world, pos));

        protect(SWAP_HAND_ITEMS, PlayerInventoryHooks.SWAP_HANDS, scope
                -> scope::isWithinScope);

        protect(ITEM_FRAME_SET_ITEM, ItemFramePutItemCallback.HOOK, scope
                -> (itemFrame, _, player, _)
                -> scope.isWithinScope(player, itemFrame));

        protect(ITEM_FRAME_REMOVE_ITEM, ItemFrameRemoveItemCallback.HOOK, scope
                -> (itemFrame, attacker)
                -> attacker instanceof ServerPlayer player && scope.isWithinScope(player, itemFrame));

        protect(ITEM_FRAME_ROTATE_ITEM, ItemFrameRotateCallback.HOOK, scope
                -> (itemFrame, player, hand)
                -> scope.isWithinScope(player, itemFrame));

        protect(ARMOR_STAND_MANIPULATE, ArmorStandManipulateCallback.HOOK, scope
                -> (armorStand, player, _, _, _)
                -> scope.isWithinScope(player, armorStand));

        protect(USE_ITEM_ON_ENTITY, ItemUseOnEntityCallback.HOOK, scope
                -> (player, entity, _, stack)
                -> scope.isWithinScope(player, entity, stack));

        protect(DESTROY_LEASH, LeashDestroyCallback.HOOK, scope
                -> scope::isWithinScope);

        protect(LEASH_ENTITIES_TO_BLOCK, LeashEntitiesToBlockCallback.HOOK, scope
                -> scope::isWithinScope);

        protect(LEASH_ENTITIES_TO_ENTITY, LeashEntitiesToEntityCallback.HOOK, scope
                -> scope::isWithinScope);

        protect(ATTACH_LEASH, LeashEntityCallback.HOOK, scope
                -> scope::isWithinScope);

        protect(DETACH_LEASH, UnleashEntityCallback.HOOK, scope
                -> scope::isWithinScope);

        protect(LEASH_KNOT_TAKE, LeashKnotTakeCallback.HOOK, scope
                -> scope::isWithinScope);

        protect(PICKUP_PROJECTILE, ProjectilePickupCallback.HOOK, scope
                -> scope::isWithinScope);

        protect(MODIFY_INVENTORY, PlayerInventoryHooks.MODIFY_INVENTORY, scope
                -> scope::isWithinScope);

        protect(EDIT_SIGN, BlockModificationHooks.EDIT_SIGN, scope
                -> (world, pos, entity)
                -> scope.isWithinScope(entity, pos));

        protect(USE_BLOCK, PlayerInteractionHooks.USE_BLOCK, scope -> (player, world, _, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (functionalBlocks.contains(state.getBlock()) && scope.isWithinScope(player, pos)) {
                if (!player.isCreative() && !player.isSpectator()) {
                    PlayerUtils.syncPlayerItems(player);
                }

                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });

        protect(DECORATED_POT_STORE, BlockModificationHooks.DECORATIVE_POT_STORE, scope
                -> (world, pos, entity)
                -> scope.isWithinScope(entity, pos));

        protect(PROJECTILE_BREAK_DECORATED_POT, ProjectileHooks.BREAK_DECORATED_POT, scope
                -> scope::isWithinScope);

        protect(ITEM_SCATTER, ItemScatterCallback.HOOK, scope
                -> scope::isWithinScope);

        protect(ENTITY_ITEM_DROP, EntityDropItemCallback.HOOK, scope
                -> (world, entity, itemEntity)
                -> scope.isWithinScope(entity, itemEntity));

        protect(CRAFT_ITEM, CraftingRecipeCallback.HOOK, scope
                -> (player, input, result)
                -> scope.isWithinScope(player, result) ? PendingResult.empty() : PendingResult.pass());

        protect(MOUNT, EntityMountCallback.HOOK, scope
                -> (entity, vehicle, force)
                -> !force && entity instanceof ServerPlayer player &&
                   scope.isWithinScope(player, vehicle));

        protect(CONSUME_FOOD, PlayerInteractionHooks.USE_ITEM, scope -> (player, _, hand) -> {
            ItemStack stack = player.getItemInHand(hand);

            if (!stack.has(DataComponents.FOOD) || !scope.isWithinScope(player, stack)) {
                return InteractionResult.PASS;
            }

            return InteractionResult.FAIL;
        });
    }

    @Override
    public void deactivate() {
        hooks.unload();
    }

    public void unload() {
        deactivate();
    }

    private Set<Block> collectFunctionalBlocks() {
        Set<Block> blocks = new HashSet<>();

        addBlocks(blocks, BlockTags.BEDS);
        addBlocks(blocks, BlockTags.CAMPFIRES);
        addBlocks(blocks, BlockTags.SHULKER_BOXES);
        addBlocks(blocks, BlockTags.ANVIL);
        blocks.add(Blocks.LECTERN);
        blocks.add(Blocks.CRAFTING_TABLE);
        blocks.add(Blocks.DROPPER);
        blocks.add(Blocks.DISPENSER);
        blocks.add(Blocks.FLETCHING_TABLE);
        blocks.add(Blocks.SMITHING_TABLE);
        blocks.add(Blocks.FURNACE);
        blocks.add(Blocks.BLAST_FURNACE);
        blocks.add(Blocks.SMOKER);
        blocks.add(Blocks.STONECUTTER);
        blocks.add(Blocks.CARTOGRAPHY_TABLE);
        blocks.add(Blocks.GRINDSTONE);
        blocks.add(Blocks.LOOM);
        blocks.add(Blocks.NOTE_BLOCK);
        blocks.add(Blocks.JUKEBOX);
        blocks.add(Blocks.CHEST);
        blocks.add(Blocks.TRAPPED_CHEST);
        blocks.add(Blocks.ENDER_CHEST);
        blocks.add(Blocks.BARREL);
        blocks.add(Blocks.ENCHANTING_TABLE);
        blocks.add(Blocks.BREWING_STAND);
        blocks.add(Blocks.BELL);
        blocks.add(Blocks.BEACON);
        blocks.add(Blocks.END_PORTAL_FRAME);
        blocks.add(Blocks.REPEATER);
        blocks.add(Blocks.COMPARATOR);
        blocks.add(Blocks.REDSTONE_WIRE);
        blocks.add(Blocks.DAYLIGHT_DETECTOR);
        blocks.add(Blocks.HOPPER);
        blocks.add(Blocks.CHISELED_BOOKSHELF);
        blocks.add(Blocks.CAULDRON);
        blocks.add(Blocks.SWEET_BERRY_BUSH);
        blocks.add(Blocks.SPAWNER);
        blocks.add(Blocks.CAVE_VINES_PLANT);
        blocks.add(Blocks.CAVE_VINES);

        return Collections.unmodifiableSet(blocks);
    }

    private void addBlocks(Set<Block> blocks, TagKey<Block> tag) {
        for (var entry : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
            Block block = entry.value();
            blocks.add(block);
        }
    }

    private static BlockModificationHooks.BlockModifyHook onModify(EntityBlockProtection.Scope scope) {
        return (_, pos, entity) -> scope.isWithinScope(entity, pos);
    }

    private <T> void protect(Protection<T> type, Consumer<T> setupAction) {
        withScope(type, setupAction);
    }

    private <T, H> void protect(Protection<T> type, Hook<H> hook, Function<T, H> listenerFactory) {
        withScope(type, scope -> {
            H listener = listenerFactory.apply(scope);
            hooks.registerHook(hook, listener);
        });
    }

    private <T> void withScope(Protection<T> type, Consumer<T> action) {
        if (!config.hasRestrictions(type)) return;

        T disallowed = config.getDisallowedScope(type);

        if (disallowed == null) return;

        T allowed = config.getAllowedScope(type);

        if (allowed == null) {
            action.accept(disallowed);
            return;
        }

        // get the resulting scope that is the difference between disallowed and allowed
        T resultingScope = type.getCombinedExcludeScope(disallowed, allowed);
        action.accept(resultingScope);
    }
}
