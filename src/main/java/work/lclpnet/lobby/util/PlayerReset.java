package work.lclpnet.lobby.util;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import work.lclpnet.kibu.access.VelocityModifier;
import work.lclpnet.kibu.hook.util.PlayerUtils;

import static net.minecraft.entity.attribute.EntityAttributes.*;

public class PlayerReset {

    private PlayerReset() {}

    public static void reset(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.ADVENTURE);
        player.clearStatusEffects();
        player.getInventory().clear();
        PlayerUtils.setCursorStack(player, ItemStack.EMPTY);

        player.getHungerManager().setFoodLevel(20);
        player.setAbsorptionAmount(0F);
        player.setExperienceLevel(0);
        player.setExperiencePoints(0);
        player.setFireTicks(0);
        player.setStuckArrowCount(0);
        player.setOnFire(false);
        VelocityModifier.setVelocity(player, Vec3d.ZERO);

        PlayerReset.resetAttributes(player);

        player.setHealth(player.getMaxHealth());
        player.dismountVehicle();

        resetSpawnPoint(player);

        PlayerAbilities abilities = player.getAbilities();
        abilities.flying = false;
        abilities.allowFlying = false;
        abilities.invulnerable = false;
        abilities.setFlySpeed(0.05f);
        modifyWalkSpeed(player, 0.1f, false);

        player.sendAbilitiesUpdate();
    }

    public static void resetSpawnPoint(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();

        if (server != null) {
            player.setSpawnPoint(World.OVERWORLD, null, 0f, true, false);
        }
    }

    public static void resetAttributes(ServerPlayerEntity player) {
        resetAttribute(player, ARMOR);
        resetAttribute(player, ARMOR_TOUGHNESS);
        resetAttribute(player, ATTACK_DAMAGE);
        resetAttribute(player, ATTACK_KNOCKBACK);
        resetAttribute(player, ATTACK_SPEED);
        resetAttribute(player, BLOCK_BREAK_SPEED);
        resetAttribute(player, BLOCK_INTERACTION_RANGE);
        resetAttribute(player, BURNING_TIME);
        resetAttribute(player, EXPLOSION_KNOCKBACK_RESISTANCE);
        resetAttribute(player, ENTITY_INTERACTION_RANGE);
        resetAttribute(player, FALL_DAMAGE_MULTIPLIER);
        resetAttribute(player, GRAVITY);
        resetAttribute(player, JUMP_STRENGTH);
        resetAttribute(player, KNOCKBACK_RESISTANCE);
        resetAttribute(player, LUCK);
        resetAttribute(player, MAX_ABSORPTION);
        resetAttribute(player, MAX_HEALTH);
        resetAttribute(player, MINING_EFFICIENCY);
        resetAttribute(player, MOVEMENT_EFFICIENCY);
        resetAttribute(player, MOVEMENT_SPEED);
        resetAttribute(player, OXYGEN_BONUS);
        resetAttribute(player, SAFE_FALL_DISTANCE);
        resetAttribute(player, SCALE);
        resetAttribute(player, SNEAKING_SPEED);
        resetAttribute(player, STEP_HEIGHT);
        resetAttribute(player, SUBMERGED_MINING_SPEED);
        resetAttribute(player, SWEEPING_DAMAGE_RATIO);
        resetAttribute(player, WATER_MOVEMENT_EFFICIENCY);
    }

    public static void resetAttribute(ServerPlayerEntity player, RegistryEntry<EntityAttribute> attribute) {
        if (attribute == MOVEMENT_SPEED) {
            setAttribute(player, attribute, player.getAbilities().getWalkSpeed());
            return;
        }

        setAttribute(player, attribute, attribute.value().getDefaultValue());
    }

    public static void setAttribute(ServerPlayerEntity player, RegistryEntry<EntityAttribute> attribute, double value) {
        EntityAttributeInstance instance = player.getAttributeInstance(attribute);

        if (instance == null) return;

        instance.setBaseValue(value);
    }

    public static void modifyWalkSpeed(ServerPlayerEntity player, float value) {
        modifyWalkSpeed(player, value, true);
    }

    public static void modifyWalkSpeed(ServerPlayerEntity player, float value, boolean update) {
        player.getAbilities().setWalkSpeed(value);

        EntityAttributeInstance attribute = player.getAttributeInstance(MOVEMENT_SPEED);

        if (attribute != null) {
            attribute.setBaseValue(value);
        }

        if (update) {
            player.sendAbilitiesUpdate();
        }
    }
}
