package work.lclpnet.lobby.game.impl.prot;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerExplosion;
import work.lclpnet.lobby.game.api.prot.Scope;
import work.lclpnet.lobby.game.impl.prot.scope.ClickEventScope;
import work.lclpnet.lobby.game.impl.prot.scope.EntityBlockScope;
import work.lclpnet.lobby.game.impl.prot.scope.EntityDamageSourceScope;
import work.lclpnet.lobby.game.impl.prot.scope.EntityItemEntityScope;
import work.lclpnet.lobby.game.impl.prot.scope.GenericScope;
import work.lclpnet.lobby.game.impl.prot.scope.ItemScatterScope;
import work.lclpnet.lobby.game.impl.prot.scope.PlayerEntityScope;
import work.lclpnet.lobby.game.impl.prot.scope.PlayerGeneric2Scope;
import work.lclpnet.lobby.game.impl.prot.scope.PlayerGenericScope;
import work.lclpnet.lobby.game.impl.prot.scope.PlayerIntBoolScope;
import work.lclpnet.lobby.game.impl.prot.scope.PlayerIntScope;
import work.lclpnet.lobby.game.impl.prot.scope.PlayerItemEntityScope;
import work.lclpnet.lobby.game.impl.prot.scope.PlayerItemStackScope;
import work.lclpnet.lobby.game.impl.prot.scope.PlayerScope;
import work.lclpnet.lobby.game.impl.prot.scope.ProjectileHitScope;
import work.lclpnet.lobby.game.impl.prot.scope.WorldBlockItemStackScope;
import work.lclpnet.lobby.game.impl.prot.scope.WorldBlockScope;

import java.util.Collection;
import java.util.Set;

public class ProtectionTypes {

    public static final EntityBlockScope BREAK_BLOCKS;
    /**
     * Block placement protection.
     * This is not considered, if {@link ProtectionTypes#USE_ITEM_ON_BLOCK} is already disallowed.
     */
    public static final EntityBlockScope PLACE_BLOCKS;
    public static final EntityBlockScope PICKUP_FLUID;
    public static final EntityBlockScope PLACE_FLUID;
    public static final PlayerGenericScope<UseOnContext> USE_ITEM_ON_BLOCK;
    public static final EntityBlockScope TRAMPLE_FARMLAND;
    public static final PlayerScope HUNGER;
    public static final EntityBlockScope MOB_GRIEFING;
    public static final EntityBlockScope CHARGE_RESPAWN_ANCHOR;
    public static final EntityBlockScope COMPOSTER;
    public static final EntityBlockScope EAT_CAKE;
    public static final EntityBlockScope EXPLODE_RESPAWN_LOCATION;
    public static final EntityBlockScope EXTINGUISH_CANDLE;
    public static final EntityBlockScope PRIME_TNT;
    public static final EntityBlockScope TAKE_LECTERN_BOOK;
    public static final EntityBlockScope TRAMPLE_TURTLE_EGG;
    public static final WorldBlockScope CAULDRON_DRIP_STONE;
    public static final GenericScope<ServerExplosion> EXPLOSION;
    public static final WorldBlockScope MELT;
    public static final WorldBlockScope FREEZE;
    public static final WorldBlockScope SNOW_FALL;
    public static final WorldBlockScope CAULDRON_PRECIPITATION;
    public static final EntityBlockScope REPLACE_DISK_ENCHANTMENT;
    public static final PlayerIntBoolScope DROP_ITEM;
    public static final EntityDamageSourceScope ALLOW_DAMAGE;
    public static final PlayerItemEntityScope PICKUP_ITEM;
    public static final WorldBlockItemStackScope BLOCK_ITEM_DROP;
    public static final WorldBlockScope BLOCK_XP_DROP;
    public static final PlayerIntScope SWAP_HAND_ITEMS;
    public static final PlayerEntityScope<ItemFrame> ITEM_FRAME_SET_ITEM;
    public static final PlayerEntityScope<ItemFrame> ITEM_FRAME_REMOVE_ITEM;
    public static final PlayerEntityScope<ItemFrame> ITEM_FRAME_ROTATE_ITEM;
    public static final PlayerEntityScope<ArmorStand> ARMOR_STAND_MANIPULATE;
    public static final PlayerGeneric2Scope<LivingEntity, ItemStack> USE_ITEM_ON_ENTITY;
    public static final PlayerEntityScope<Entity> DESTROY_LEASH;
    public static final PlayerEntityScope<Entity> ATTACH_LEASH;
    public static final PlayerEntityScope<Entity> DETACH_LEASH;
    public static final PlayerEntityScope<LeashFenceKnotEntity> LEASH_KNOT_TAKE;
    public static final PlayerGeneric2Scope<BlockPos, Collection<Entity>> LEASH_ENTITIES_TO_BLOCK;
    public static final PlayerGeneric2Scope<Entity, Collection<Entity>> LEASH_ENTITIES_TO_ENTITY;
    public static final PlayerEntityScope<Projectile> PICKUP_PROJECTILE;
    public static final ClickEventScope MODIFY_INVENTORY;
    public static final EntityBlockScope EDIT_SIGN;
    /**
     * Block usage protection.
     * Only used for blocks that have special functionality, e.g. beds, chests, crafting tables etc.
     * {@link work.lclpnet.lobby.game.api.prot.Protector} implementations can choose which blocks should be accounted for.
     */
    public static final EntityBlockScope USE_BLOCK;
    public static final EntityBlockScope DECORATED_POT_STORE;
    public static final ProjectileHitScope PROJECTILE_BREAK_DECORATED_POT;
    public static final ItemScatterScope ITEM_SCATTER;
    public static final EntityItemEntityScope ENTITY_ITEM_DROP;
    public static final PlayerItemStackScope CRAFT_ITEM;
    public static final PlayerEntityScope<Entity> MOUNT;
    public static final PlayerItemStackScope CONSUME_FOOD;

    private static final Set<Scope<?>> types;

    static {
        types = ImmutableSet.<Scope<?>>builder()
                .add(BREAK_BLOCKS = new EntityBlockScope())
                .add(PLACE_BLOCKS = new EntityBlockScope())
                .add(PICKUP_FLUID = new EntityBlockScope())
                .add(PLACE_FLUID = new EntityBlockScope())
                .add(USE_ITEM_ON_BLOCK = new PlayerGenericScope<>())
                .add(TRAMPLE_FARMLAND = new EntityBlockScope())
                .add(HUNGER = new PlayerScope())
                .add(MOB_GRIEFING = new EntityBlockScope())
                .add(CHARGE_RESPAWN_ANCHOR = new EntityBlockScope())
                .add(COMPOSTER = new EntityBlockScope())
                .add(EAT_CAKE = new EntityBlockScope())
                .add(EXPLODE_RESPAWN_LOCATION = new EntityBlockScope())
                .add(EXTINGUISH_CANDLE = new EntityBlockScope())
                .add(PRIME_TNT = new EntityBlockScope())
                .add(TAKE_LECTERN_BOOK = new EntityBlockScope())
                .add(TRAMPLE_TURTLE_EGG = new EntityBlockScope())
                .add(CAULDRON_DRIP_STONE = new WorldBlockScope())
                .add(EXPLOSION = new GenericScope<>())
                .add(MELT = new WorldBlockScope())
                .add(FREEZE = new WorldBlockScope())
                .add(SNOW_FALL = new WorldBlockScope())
                .add(CAULDRON_PRECIPITATION = new WorldBlockScope())
                .add(REPLACE_DISK_ENCHANTMENT = new EntityBlockScope())
                .add(DROP_ITEM = new PlayerIntBoolScope())
                .add(ALLOW_DAMAGE = new EntityDamageSourceScope())
                .add(PICKUP_ITEM = new PlayerItemEntityScope())
                .add(BLOCK_ITEM_DROP = new WorldBlockItemStackScope())
                .add(BLOCK_XP_DROP = new WorldBlockScope())
                .add(SWAP_HAND_ITEMS = new PlayerIntScope())
                .add(ITEM_FRAME_SET_ITEM = new PlayerEntityScope<>())
                .add(ITEM_FRAME_REMOVE_ITEM = new PlayerEntityScope<>())
                .add(ITEM_FRAME_ROTATE_ITEM = new PlayerEntityScope<>())
                .add(ARMOR_STAND_MANIPULATE = new PlayerEntityScope<>())
                .add(USE_ITEM_ON_ENTITY = new PlayerGeneric2Scope<>())
                .add(ATTACH_LEASH = new PlayerEntityScope<>())
                .add(DETACH_LEASH = new PlayerEntityScope<>())
                .add(LEASH_KNOT_TAKE = new PlayerEntityScope<>())
                .add(LEASH_ENTITIES_TO_BLOCK = new PlayerGeneric2Scope<>())
                .add(LEASH_ENTITIES_TO_ENTITY = new PlayerGeneric2Scope<>())
                .add(DESTROY_LEASH = new PlayerEntityScope<>())
                .add(PICKUP_PROJECTILE = new PlayerEntityScope<>())
                .add(MODIFY_INVENTORY = new ClickEventScope())
                .add(EDIT_SIGN = new EntityBlockScope())
                .add(USE_BLOCK = new EntityBlockScope())
                .add(DECORATED_POT_STORE = new EntityBlockScope())
                .add(PROJECTILE_BREAK_DECORATED_POT = new ProjectileHitScope())
                .add(ITEM_SCATTER = new ItemScatterScope())
                .add(ENTITY_ITEM_DROP = new EntityItemEntityScope())
                .add(CRAFT_ITEM = new PlayerItemStackScope())
                .add(MOUNT = new PlayerEntityScope<>())
                .add(CONSUME_FOOD = new PlayerItemStackScope())
                .build();
    }

    private ProtectionTypes() {}

    public static Set<Scope<?>> getTypes() {
        return types;
    }
}
