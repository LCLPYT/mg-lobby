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
                .add(BREAK_BLOCKS = EntityBlockProtectionType.INSTANCE)
                .add(PLACE_BLOCKS = EntityBlockProtectionType.INSTANCE)
                .add(PICKUP_FLUID = EntityBlockProtectionType.INSTANCE)
                .add(PLACE_FLUID = EntityBlockProtectionType.INSTANCE)
                .add(USE_ITEM_ON_BLOCK = EntityBlockProtectionType.INSTANCE)
                .add(TRAMPLE_FARMLAND = EntityBlockProtectionType.INSTANCE)
                .add(HUNGER = PlayerProtectionType.INSTANCE)
                .add(MOB_GRIEFING = EntityBlockProtectionType.INSTANCE)
                .add(CHARGE_RESPAWN_ANCHOR = EntityBlockProtectionType.INSTANCE)
                .add(COMPOSTER = EntityBlockProtectionType.INSTANCE)
                .add(EAT_CAKE = EntityBlockProtectionType.INSTANCE)
                .add(EXPLODE_RESPAWN_LOCATION = EntityBlockProtectionType.INSTANCE)
                .add(EXTINGUISH_CANDLE = EntityBlockProtectionType.INSTANCE)
                .add(PRIME_TNT = EntityBlockProtectionType.INSTANCE)
                .add(TAKE_LECTERN_BOOK = EntityBlockProtectionType.INSTANCE)
                .add(TRAMPLE_TURTLE_EGG = EntityBlockProtectionType.INSTANCE)
                .add(CAULDRON_DRIP_STONE = WorldBlockProtectionType.INSTANCE)
                .add(EXPLOSION = EntityBlockProtectionType.INSTANCE)
                .add(MELT = WorldBlockProtectionType.INSTANCE)
                .add(FREEZE = WorldBlockProtectionType.INSTANCE)
                .add(SNOW_FALL = WorldBlockProtectionType.INSTANCE)
                .add(CAULDRON_PRECIPITATION = WorldBlockProtectionType.INSTANCE)
                .add(FROST_WALKER_FREEZE = EntityBlockProtectionType.INSTANCE)
                .add(DROP_ITEM = PlayerIntProtectionType.INSTANCE)
                .add(ALLOW_DAMAGE = EntityDamageSourceProtectionType.INSTANCE)
                .add(PICKUP_ITEM = PlayerItemEntityProtectionType.INSTANCE)
                .add(BLOCK_ITEM_DROP = WorldBlockItemStackProtectionType.INSTANCE)
                .add(BLOCK_XP_DROP = WorldBlockProtectionType.INSTANCE)
                .add(SWAP_HAND_ITEMS = PlayerIntProtectionType.INSTANCE)
                .add(ITEM_FRAME_SET_ITEM = PlayerEntityProtectionType.instance())
                .add(ITEM_FRAME_REMOVE_ITEM = PlayerEntityProtectionType.instance())
                .add(ITEM_FRAME_ROTATE_ITEM = PlayerEntityProtectionType.instance())
                .add(ARMOR_STAND_MANIPULATE = PlayerEntityProtectionType.instance())
                .add(USE_ITEM_ON_ENTITY = PlayerEntityProtectionType.instance())
                .add(ATTACH_LEASH = EntityBlockProtectionType.INSTANCE)
                .add(DETACH_LEASH = PlayerEntityProtectionType.instance())
                .add(LEASH_MOB = PlayerEntityProtectionType.instance())
                .add(UNLEASH_MOB = PlayerEntityProtectionType.instance())
                .add(LEASH_MOB_TO_BLOCK = PlayerEntityProtectionType.instance())
                .add(PICKUP_PROJECTILE = PlayerEntityProtectionType.instance())
                .add(MODIFY_INVENTORY = ClickEventProtectionType.INSTANCE)
                .add(EDIT_SIGN = EntityBlockProtectionType.INSTANCE)
                .build();
    }

    private ProtectionTypes() {}

    public static Set<ProtectionType<?>> getTypes() {
        return types;
    }
}
