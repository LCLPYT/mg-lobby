package work.lclpnet.lobby.game.impl.prot;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.*;
import work.lclpnet.lobby.game.impl.prot.type.*;

import java.util.Set;

public class ProtectionTypes {

    public static final ProtectionType<EntityBlockScope> BREAK_BLOCKS;
    /**
     * Block placement protection.
     * This is not considered, if {@link ProtectionTypes#USE_ITEM_ON_BLOCK} is already disallowed.
     */
    public static final ProtectionType<EntityBlockScope> PLACE_BLOCKS;
    public static final ProtectionType<EntityBlockScope> PICKUP_FLUID;
    public static final ProtectionType<EntityBlockScope> PLACE_FLUID;
    public static final ProtectionType<EntityBlockScope> USE_ITEM_ON_BLOCK;
    public static final ProtectionType<EntityBlockScope> TRAMPLE_FARMLAND;
    public static final ProtectionType<PlayerScope> HUNGER;
    public static final ProtectionType<EntityBlockScope> MOB_GRIEFING;
    public static final ProtectionType<EntityBlockScope> CHARGE_RESPAWN_ANCHOR;
    public static final ProtectionType<EntityBlockScope> COMPOSTER;
    public static final ProtectionType<EntityBlockScope> EAT_CAKE;
    public static final ProtectionType<EntityBlockScope> EXPLODE_RESPAWN_LOCATION;
    public static final ProtectionType<EntityBlockScope> EXTINGUISH_CANDLE;
    public static final ProtectionType<EntityBlockScope> PRIME_TNT;
    public static final ProtectionType<EntityBlockScope> TAKE_LECTERN_BOOK;
    public static final ProtectionType<EntityBlockScope> TRAMPLE_TURTLE_EGG;
    public static final ProtectionType<WorldBlockScope> CAULDRON_DRIP_STONE;
    public static final ProtectionType<EntityBlockScope> EXPLOSION;
    public static final ProtectionType<WorldBlockScope> MELT;
    public static final ProtectionType<WorldBlockScope> FREEZE;
    public static final ProtectionType<WorldBlockScope> SNOW_FALL;
    public static final ProtectionType<WorldBlockScope> CAULDRON_PRECIPITATION;
    public static final ProtectionType<EntityBlockScope> FROST_WALKER_FREEZE;
    public static final ProtectionType<PlayerIntScope> DROP_ITEM;
    public static final ProtectionType<EntityDamageSourceScope> ALLOW_DAMAGE;
    public static final ProtectionType<PlayerItemEntityScope> PICKUP_ITEM;
    public static final ProtectionType<WorldBlockItemStackScope> BLOCK_ITEM_DROP;
    public static final ProtectionType<WorldBlockScope> BLOCK_XP_DROP;
    public static final ProtectionType<PlayerIntScope> SWAP_HAND_ITEMS;
    public static final ProtectionType<PlayerEntityScope<ItemFrameEntity>> ITEM_FRAME_SET_ITEM;
    public static final ProtectionType<PlayerEntityScope<ItemFrameEntity>> ITEM_FRAME_REMOVE_ITEM;
    public static final ProtectionType<PlayerEntityScope<ItemFrameEntity>> ITEM_FRAME_ROTATE_ITEM;
    public static final ProtectionType<PlayerEntityScope<ArmorStandEntity>> ARMOR_STAND_MANIPULATE;
    public static final ProtectionType<PlayerEntityScope<LivingEntity>> USE_ITEM_ON_ENTITY;
    public static final ProtectionType<EntityBlockScope> ATTACH_LEASH;
    public static final ProtectionType<PlayerEntityScope<LeashKnotEntity>> DETACH_LEASH;
    public static final ProtectionType<PlayerEntityScope<MobEntity>> LEASH_MOB;
    public static final ProtectionType<PlayerEntityScope<MobEntity>> UNLEASH_MOB;
    public static final ProtectionType<PlayerEntityScope<MobEntity>> LEASH_MOB_TO_BLOCK;
    public static final ProtectionType<PlayerEntityScope<ProjectileEntity>> PICKUP_PROJECTILE;
    public static final ProtectionType<ClickEventScope> MODIFY_INVENTORY;
    public static final ProtectionType<EntityBlockScope> EDIT_SIGN;

    private static final Set<ProtectionType<?>> types;

    static {
        types = ImmutableSet.<ProtectionType<?>>builder()
                .add(BREAK_BLOCKS = new EntityBlockProtectionType())
                .add(PLACE_BLOCKS = new EntityBlockProtectionType())
                .add(PICKUP_FLUID = new EntityBlockProtectionType())
                .add(PLACE_FLUID = new EntityBlockProtectionType())
                .add(USE_ITEM_ON_BLOCK = new EntityBlockProtectionType())
                .add(TRAMPLE_FARMLAND = new EntityBlockProtectionType())
                .add(HUNGER = new PlayerProtectionType())
                .add(MOB_GRIEFING = new EntityBlockProtectionType())
                .add(CHARGE_RESPAWN_ANCHOR = new EntityBlockProtectionType())
                .add(COMPOSTER = new EntityBlockProtectionType())
                .add(EAT_CAKE = new EntityBlockProtectionType())
                .add(EXPLODE_RESPAWN_LOCATION = new EntityBlockProtectionType())
                .add(EXTINGUISH_CANDLE = new EntityBlockProtectionType())
                .add(PRIME_TNT = new EntityBlockProtectionType())
                .add(TAKE_LECTERN_BOOK = new EntityBlockProtectionType())
                .add(TRAMPLE_TURTLE_EGG = new EntityBlockProtectionType())
                .add(CAULDRON_DRIP_STONE = new WorldBlockProtectionType())
                .add(EXPLOSION = new EntityBlockProtectionType())
                .add(MELT = new WorldBlockProtectionType())
                .add(FREEZE = new WorldBlockProtectionType())
                .add(SNOW_FALL = new WorldBlockProtectionType())
                .add(CAULDRON_PRECIPITATION = new WorldBlockProtectionType())
                .add(FROST_WALKER_FREEZE = new EntityBlockProtectionType())
                .add(DROP_ITEM = new PlayerIntProtectionType())
                .add(ALLOW_DAMAGE = new EntityDamageSourceProtectionType())
                .add(PICKUP_ITEM = new PlayerItemEntityProtectionType())
                .add(BLOCK_ITEM_DROP = new WorldBlockItemStackProtectionType())
                .add(BLOCK_XP_DROP = new WorldBlockProtectionType())
                .add(SWAP_HAND_ITEMS = new PlayerIntProtectionType())
                .add(ITEM_FRAME_SET_ITEM = new PlayerEntityProtectionType<>())
                .add(ITEM_FRAME_REMOVE_ITEM = new PlayerEntityProtectionType<>())
                .add(ITEM_FRAME_ROTATE_ITEM = new PlayerEntityProtectionType<>())
                .add(ARMOR_STAND_MANIPULATE = new PlayerEntityProtectionType<>())
                .add(USE_ITEM_ON_ENTITY = new PlayerEntityProtectionType<>())
                .add(ATTACH_LEASH = new EntityBlockProtectionType())
                .add(DETACH_LEASH = new PlayerEntityProtectionType<>())
                .add(LEASH_MOB = new PlayerEntityProtectionType<>())
                .add(UNLEASH_MOB = new PlayerEntityProtectionType<>())
                .add(LEASH_MOB_TO_BLOCK = new PlayerEntityProtectionType<>())
                .add(PICKUP_PROJECTILE = new PlayerEntityProtectionType<>())
                .add(MODIFY_INVENTORY = new ClickEventProtectionType())
                .add(EDIT_SIGN = new EntityBlockProtectionType())
                .build();
    }

    private ProtectionTypes() {}

    public static Set<ProtectionType<?>> getTypes() {
        return types;
    }
}
