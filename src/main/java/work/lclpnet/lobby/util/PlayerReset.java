package work.lclpnet.lobby.util;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
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
        resetAttribute(player, EntityAttributes.GENERIC_ARMOR);
        resetAttribute(player, EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        resetAttribute(player, EntityAttributes.GENERIC_ATTACK_DAMAGE);
        resetAttribute(player, EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        resetAttribute(player, EntityAttributes.GENERIC_ATTACK_SPEED);
        resetAttribute(player, EntityAttributes.GENERIC_FLYING_SPEED);
        resetAttribute(player, EntityAttributes.GENERIC_FOLLOW_RANGE);
        resetAttribute(player, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        resetAttribute(player, EntityAttributes.GENERIC_LUCK);
        resetAttribute(player, EntityAttributes.GENERIC_MAX_ABSORPTION);
        resetAttribute(player, EntityAttributes.GENERIC_MAX_HEALTH);
        resetAttribute(player, EntityAttributes.GENERIC_MOVEMENT_SPEED);
        resetAttribute(player, EntityAttributes.GENERIC_SCALE);
        resetAttribute(player, EntityAttributes.GENERIC_STEP_HEIGHT);
        resetAttribute(player, EntityAttributes.GENERIC_JUMP_STRENGTH);
        resetAttribute(player, EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE);
        resetAttribute(player, EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
        resetAttribute(player, EntityAttributes.PLAYER_BLOCK_BREAK_SPEED);
        resetAttribute(player, EntityAttributes.GENERIC_GRAVITY);
        resetAttribute(player, EntityAttributes.GENERIC_SAFE_FALL_DISTANCE);
        resetAttribute(player, EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER);
    }

    public static void resetAttribute(ServerPlayerEntity player, RegistryEntry<EntityAttribute> attribute) {
        if (attribute == EntityAttributes.GENERIC_MOVEMENT_SPEED) {
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

        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);

        if (attribute != null) {
            attribute.setBaseValue(value);
        }

        if (update) {
            player.sendAbilitiesUpdate();
        }
    }
}
