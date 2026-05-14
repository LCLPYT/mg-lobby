package work.lclpnet.game.impl.prot;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ServerExplosion;
import work.lclpnet.game.api.prot.Protection;
import work.lclpnet.game.api.prot.Protector;
import work.lclpnet.game.impl.prot.scope.*;

import java.util.Collection;
import java.util.Set;

public class ProtectionTypes {

    public static final EntityBlockProtection BREAK_BLOCKS;
    /**
     * Block placement protection.
     * This is not considered, if {@link ProtectionTypes#USE_ITEM_ON_BLOCK} is already disallowed.
     */
    public static final EntityBlockProtection PLACE_BLOCKS;
    public static final EntityBlockProtection PICKUP_FLUID;
    public static final EntityBlockProtection PLACE_FLUID;
    public static final PlayerGenericProtection<UseOnContext> USE_ITEM_ON_BLOCK;
    public static final EntityBlockProtection TRAMPLE_FARMLAND;
    public static final PlayerProtection HUNGER;
    public static final EntityBlockProtection MOB_GRIEFING;
    public static final EntityBlockProtection CHARGE_RESPAWN_ANCHOR;
    public static final EntityBlockProtection COMPOSTER;
    public static final EntityBlockProtection EAT_CAKE;
    public static final EntityBlockProtection EXPLODE_RESPAWN_LOCATION;
    public static final EntityBlockProtection EXTINGUISH_CANDLE;
    public static final EntityBlockProtection PRIME_TNT;
    public static final EntityBlockProtection TAKE_LECTERN_BOOK;
    public static final EntityBlockProtection TRAMPLE_TURTLE_EGG;
    public static final WorldBlockProtection CAULDRON_DRIP_STONE;
    public static final GenericProtection<ServerExplosion> EXPLOSION;
    public static final WorldBlockProtection MELT;
    public static final WorldBlockProtection FREEZE;
    public static final WorldBlockProtection SNOW_FALL;
    public static final WorldBlockProtection CAULDRON_PRECIPITATION;
    public static final EntityBlockProtection REPLACE_DISK_ENCHANTMENT;
    public static final PlayerIntBoolProtection DROP_ITEM;
    public static final EntityDamageSourceProtection ALLOW_DAMAGE;
    public static final PlayerItemEntityProtection PICKUP_ITEM;
    public static final WorldBlockItemStackProtection BLOCK_ITEM_DROP;
    public static final WorldBlockProtection BLOCK_XP_DROP;
    public static final PlayerIntProtection SWAP_HAND_ITEMS;
    public static final PlayerEntityProtection<ItemFrame> ITEM_FRAME_SET_ITEM;
    public static final PlayerEntityProtection<ItemFrame> ITEM_FRAME_REMOVE_ITEM;
    public static final PlayerEntityProtection<ItemFrame> ITEM_FRAME_ROTATE_ITEM;
    public static final PlayerEntityProtection<ArmorStand> ARMOR_STAND_MANIPULATE;
    public static final PlayerGeneric2Protection<LivingEntity, ItemStack> USE_ITEM_ON_ENTITY;
    public static final PlayerEntityProtection<Entity> DESTROY_LEASH;
    public static final PlayerEntityProtection<Entity> ATTACH_LEASH;
    public static final PlayerEntityProtection<Entity> DETACH_LEASH;
    public static final PlayerEntityProtection<LeashFenceKnotEntity> LEASH_KNOT_TAKE;
    public static final PlayerGeneric2Protection<BlockPos, Collection<Entity>> LEASH_ENTITIES_TO_BLOCK;
    public static final PlayerGeneric2Protection<Entity, Collection<Entity>> LEASH_ENTITIES_TO_ENTITY;
    public static final PlayerEntityProtection<Projectile> PICKUP_PROJECTILE;
    public static final ClickEventProtection MODIFY_INVENTORY;
    public static final EntityBlockProtection EDIT_SIGN;
    /**
     * Block usage protection.
     * Only used for blocks that have special functionality, e.g. beds, chests, crafting tables etc.
     * {@link Protector} implementations can choose which blocks should be accounted for.
     */
    public static final EntityBlockProtection USE_BLOCK;
    public static final EntityBlockProtection DECORATED_POT_STORE;
    public static final ProjectileHitProtection PROJECTILE_BREAK_DECORATED_POT;
    public static final ItemScatterProtection ITEM_SCATTER;
    public static final EntityItemEntityProtection ENTITY_ITEM_DROP;
    public static final PlayerItemStackProtection CRAFT_ITEM;
    public static final PlayerEntityProtection<Entity> MOUNT;
    public static final PlayerItemStackProtection CONSUME_FOOD;

    private static final Set<Protection<?>> types;

    static {
        types = ImmutableSet.<Protection<?>>builder()
                .add(BREAK_BLOCKS = new EntityBlockProtection())
                .add(PLACE_BLOCKS = new EntityBlockProtection())
                .add(PICKUP_FLUID = new EntityBlockProtection())
                .add(PLACE_FLUID = new EntityBlockProtection())
                .add(USE_ITEM_ON_BLOCK = new PlayerGenericProtection<>())
                .add(TRAMPLE_FARMLAND = new EntityBlockProtection())
                .add(HUNGER = new PlayerProtection())
                .add(MOB_GRIEFING = new EntityBlockProtection())
                .add(CHARGE_RESPAWN_ANCHOR = new EntityBlockProtection())
                .add(COMPOSTER = new EntityBlockProtection())
                .add(EAT_CAKE = new EntityBlockProtection())
                .add(EXPLODE_RESPAWN_LOCATION = new EntityBlockProtection())
                .add(EXTINGUISH_CANDLE = new EntityBlockProtection())
                .add(PRIME_TNT = new EntityBlockProtection())
                .add(TAKE_LECTERN_BOOK = new EntityBlockProtection())
                .add(TRAMPLE_TURTLE_EGG = new EntityBlockProtection())
                .add(CAULDRON_DRIP_STONE = new WorldBlockProtection())
                .add(EXPLOSION = new GenericProtection<>())
                .add(MELT = new WorldBlockProtection())
                .add(FREEZE = new WorldBlockProtection())
                .add(SNOW_FALL = new WorldBlockProtection())
                .add(CAULDRON_PRECIPITATION = new WorldBlockProtection())
                .add(REPLACE_DISK_ENCHANTMENT = new EntityBlockProtection())
                .add(DROP_ITEM = new PlayerIntBoolProtection())
                .add(ALLOW_DAMAGE = new EntityDamageSourceProtection())
                .add(PICKUP_ITEM = new PlayerItemEntityProtection())
                .add(BLOCK_ITEM_DROP = new WorldBlockItemStackProtection())
                .add(BLOCK_XP_DROP = new WorldBlockProtection())
                .add(SWAP_HAND_ITEMS = new PlayerIntProtection())
                .add(ITEM_FRAME_SET_ITEM = new PlayerEntityProtection<>())
                .add(ITEM_FRAME_REMOVE_ITEM = new PlayerEntityProtection<>())
                .add(ITEM_FRAME_ROTATE_ITEM = new PlayerEntityProtection<>())
                .add(ARMOR_STAND_MANIPULATE = new PlayerEntityProtection<>())
                .add(USE_ITEM_ON_ENTITY = new PlayerGeneric2Protection<>())
                .add(ATTACH_LEASH = new PlayerEntityProtection<>())
                .add(DETACH_LEASH = new PlayerEntityProtection<>())
                .add(LEASH_KNOT_TAKE = new PlayerEntityProtection<>())
                .add(LEASH_ENTITIES_TO_BLOCK = new PlayerGeneric2Protection<>())
                .add(LEASH_ENTITIES_TO_ENTITY = new PlayerGeneric2Protection<>())
                .add(DESTROY_LEASH = new PlayerEntityProtection<>())
                .add(PICKUP_PROJECTILE = new PlayerEntityProtection<>())
                .add(MODIFY_INVENTORY = new ClickEventProtection())
                .add(EDIT_SIGN = new EntityBlockProtection())
                .add(USE_BLOCK = new EntityBlockProtection())
                .add(DECORATED_POT_STORE = new EntityBlockProtection())
                .add(PROJECTILE_BREAK_DECORATED_POT = new ProjectileHitProtection())
                .add(ITEM_SCATTER = new ItemScatterProtection())
                .add(ENTITY_ITEM_DROP = new EntityItemEntityProtection())
                .add(CRAFT_ITEM = new PlayerItemStackProtection())
                .add(MOUNT = new PlayerEntityProtection<>())
                .add(CONSUME_FOOD = new PlayerItemStackProtection())
                .build();
    }

    private ProtectionTypes() {}

    public static Set<Protection<?>> getTypes() {
        return types;
    }
}
