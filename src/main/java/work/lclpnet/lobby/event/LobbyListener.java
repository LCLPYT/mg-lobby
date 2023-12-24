package work.lclpnet.lobby.event;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.access.VelocityModifier;
import work.lclpnet.kibu.hook.ServerPlayConnectionHooks;
import work.lclpnet.kibu.hook.entity.PlayerInteractionHooks;
import work.lclpnet.kibu.hook.player.PlayerAdvancementPacketCallback;
import work.lclpnet.kibu.hook.player.PlayerMoveCallback;
import work.lclpnet.kibu.hook.player.PlayerRecipePacketCallback;
import work.lclpnet.kibu.hook.util.PositionRotation;
import work.lclpnet.kibu.plugin.hook.HookListenerModule;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.config.LobbyWorldConfig;

import javax.inject.Inject;
import java.util.Set;

public class LobbyListener implements HookListenerModule {

    private final LobbyManager lobbyManager;
    private final Scheduler scheduler;
    private final LobbyWorldConfig config;

    @Inject
    public LobbyListener(LobbyManager lobbyManager, Scheduler scheduler, LobbyWorldConfig config) {
        this.lobbyManager = lobbyManager;
        this.scheduler = scheduler;
        this.config = config;
    }

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(ServerPlayConnectionHooks.JOIN, this::onJoin);
        registrar.registerHook(PlayerMoveCallback.HOOK, this::onMove);
        registrar.registerHook(PlayerInteractionHooks.ATTACK_ENTITY, this::onAttack);
        registrar.registerHook(PlayerAdvancementPacketCallback.HOOK, (player, packet) -> true);
        registrar.registerHook(PlayerRecipePacketCallback.HOOK, (player, packet) -> true);
    }

    @SuppressWarnings("SameReturnValue")
    private ActionResult onAttack(PlayerEntity player, World world, Hand hand, Entity entity,
                                  @Nullable EntityHitResult hitResult) {

        if (!isLobby(world) || !(entity instanceof ServerPlayerEntity target) || !(world instanceof ServerWorld serverWorld)) {
            return ActionResult.PASS;
        }

        VelocityModifier.setVelocity(target, player.getRotationVector().multiply(0.25));

        serverWorld.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(), target.getZ(),
                1, 0.1, 0, 0.1, 0.1);

        return ActionResult.PASS;
    }

    private boolean onMove(ServerPlayerEntity player, PositionRotation from, PositionRotation to) {
        ServerWorld world = player.getServerWorld();

        if (isLobby(world)) {
            onLobbyMove(player, to, world);
        }

        return false;  // false = allow movement
    }

    private void onLobbyMove(ServerPlayerEntity player, PositionRotation to, ServerWorld world) {
        if (to.getY() < world.getBottomY()) {
            // teleport player back to spawn location
            Vec3d spawn = lobbyManager.getLobbySpawn();

            player.teleport(world, spawn.getX(), spawn.getY(), spawn.getZ(), Set.of(), 0, 0);
            return;
        }

        handleLavaLevitation(player, to, world);
    }

    private void handleLavaLevitation(ServerPlayerEntity player, PositionRotation to, ServerWorld world) {
        if (config.lavaLevitation == null) return;

        BlockPos pos = player.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (player.isSpectator() || !state.isOf(Blocks.LAVA) || player.hasStatusEffect(StatusEffects.LEVITATION)) return;

        if (!config.lavaLevitation.isWithinBounds(pos.getX(), pos.getY(), pos.getZ())) return;

        int durationTicks = config.lavaLevitation.durationTicks();

        StatusEffectInstance effect = new StatusEffectInstance(StatusEffects.LEVITATION, durationTicks, 8,
                false, false);

        player.addStatusEffect(effect);

        VelocityModifier.setVelocity(player, new Vec3d(0, 1, 0));

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, SoundCategory.PLAYERS, 0.2f, 1f);

        startLevitationTask(player, world);
    }

    private void startLevitationTask(ServerPlayerEntity player, ServerWorld world) {
        scheduler.interval(action -> {
            double x = player.getX(), y = player.getY(), z = player.getZ();

            player.setFireTicks(0);

            if (!player.hasStatusEffect(StatusEffects.LEVITATION)) {
                world.spawnParticles(ParticleTypes.DRAGON_BREATH, x, y, z, 50, 0.1, 0.1, 0.1, 0.05);
                action.cancel();
                return;
            }

            world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0);
        }, 1);
    }

    private void onJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        lobbyManager.sendToLobby(handler.player);
    }

    private boolean isLobby(World world) {
        return lobbyManager.getLobbyWorld() == world;
    }
}
