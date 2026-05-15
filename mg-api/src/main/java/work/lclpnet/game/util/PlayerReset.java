package work.lclpnet.game.util;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import work.lclpnet.kibu.access.VelocityModifier;
import work.lclpnet.kibu.hook.util.PlayerUtils;

import static net.minecraft.world.entity.ai.attributes.Attributes.*;

public class PlayerReset {

    private PlayerReset() {}

    public static void reset(ServerPlayer player) {
        player.setGameMode(GameType.ADVENTURE);
        player.removeAllEffects();
        player.getInventory().clearContent();
        PlayerUtils.setCursorStack(player, ItemStack.EMPTY);

        player.getFoodData().setFoodLevel(20);
        player.setAbsorptionAmount(0F);
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);
        player.setRemainingFireTicks(0);
        player.setArrowCount(0);
        player.setSharedFlagOnFire(false);
        VelocityModifier.setVelocity(player, Vec3.ZERO);

        PlayerReset.resetAttributes(player);

        player.setHealth(player.getMaxHealth());
        player.removeVehicle();

        resetSpawnPoint(player);

        Abilities abilities = player.getAbilities();
        abilities.flying = false;
        abilities.mayfly = false;
        abilities.invulnerable = false;
        abilities.setFlyingSpeed(0.05f);
        modifyWalkSpeed(player, 0.1f, false);

        player.onUpdateAbilities();
    }

    public static void resetSpawnPoint(ServerPlayer player) {
        player.setRespawnPosition(null, false);
    }

    public static void resetAttributes(ServerPlayer player) {
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

    public static void resetAttribute(ServerPlayer player, Holder<Attribute> attribute) {
        if (attribute == MOVEMENT_SPEED) {
            setAttribute(player, attribute, player.getAbilities().getWalkingSpeed());
            return;
        }

        setAttribute(player, attribute, attribute.value().getDefaultValue());
    }

    public static void setAttribute(ServerPlayer player, Holder<Attribute> attribute, double value) {
        AttributeInstance instance = player.getAttribute(attribute);

        if (instance == null) return;

        instance.setBaseValue(value);
    }

    public static void modifyWalkSpeed(ServerPlayer player, float value) {
        modifyWalkSpeed(player, value, true);
    }

    public static void modifyWalkSpeed(ServerPlayer player, float value, boolean update) {
        player.getAbilities().setWalkingSpeed(value);

        AttributeInstance attribute = player.getAttribute(MOVEMENT_SPEED);

        if (attribute != null) {
            attribute.setBaseValue(value);
        }

        if (update) {
            player.onUpdateAbilities();
        }
    }
}
