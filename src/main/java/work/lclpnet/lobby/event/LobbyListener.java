package work.lclpnet.lobby.event;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.access.VelocityModifier;
import work.lclpnet.kibu.hook.HookListenerModule;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.ServerPlayConnectionHooks;
import work.lclpnet.kibu.hook.entity.PlayerInteractionHooks;
import work.lclpnet.kibu.hook.player.PlayerAdvancementPacketCallback;
import work.lclpnet.kibu.hook.player.PlayerMoveCallback;
import work.lclpnet.kibu.hook.player.PlayerRecipeNotificationCallback;
import work.lclpnet.kibu.hook.player.PlayerWaypointCallback;
import work.lclpnet.kibu.hook.util.PositionRotation;
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
        registrar.registerHook(PlayerRecipeNotificationCallback.HOOK, (player, entry, displayEntry) -> true);
        registrar.registerHook(PlayerWaypointCallback.HOOK, (player, waypoint) -> true);
    }

    @SuppressWarnings("SameReturnValue")
    private InteractionResult onAttack(Player player, Level world, InteractionHand hand, Entity entity,
                                       @Nullable EntityHitResult hitResult) {

        if (!isLobby(world) || !(entity instanceof ServerPlayer target) || !(world instanceof ServerLevel serverWorld)) {
            return InteractionResult.PASS;
        }

        VelocityModifier.setVelocity(target, player.getLookAngle().scale(0.25));

        serverWorld.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(), target.getZ(),
                1, 0.1, 0, 0.1, 0.1);

        return InteractionResult.PASS;
    }

    private boolean onMove(ServerPlayer player, PositionRotation from, PositionRotation to) {
        ServerLevel world = player.level();

        if (isLobby(world)) {
            onLobbyMove(player, to, world);
        }

        return false;  // false = allow movement
    }

    private void onLobbyMove(ServerPlayer player, PositionRotation to, ServerLevel world) {
        if (to.y() < world.getMinY()) {
            // teleport player back to spawn location
            Vec3 spawn = lobbyManager.getLobbySpawn();

            player.teleportTo(world, spawn.x(), spawn.y(), spawn.z(), Set.of(), 0, 0, true);
            return;
        }

        handleLavaLevitation(player, to, world);
    }

    private void handleLavaLevitation(ServerPlayer player, PositionRotation to, ServerLevel world) {
        if (config.lavaLevitation == null) return;

        BlockPos pos = player.blockPosition();
        BlockState state = world.getBlockState(pos);

        if (player.isSpectator() || !state.is(Blocks.LAVA) || player.hasEffect(MobEffects.LEVITATION)) return;

        if (!config.lavaLevitation.isWithinBounds(pos.getX(), pos.getY(), pos.getZ())) return;

        int durationTicks = config.lavaLevitation.durationTicks();

        MobEffectInstance effect = new MobEffectInstance(MobEffects.LEVITATION, durationTicks, 8,
                false, false);

        player.addEffect(effect);

        VelocityModifier.setVelocity(player, new Vec3(0, 1, 0));

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ILLUSIONER_PREPARE_BLINDNESS, SoundSource.PLAYERS, 0.2f, 1f);

        startLevitationTask(player, world);
    }

    private void startLevitationTask(ServerPlayer player, ServerLevel world) {
        scheduler.interval(action -> {
            double x = player.getX(), y = player.getY(), z = player.getZ();

            player.setRemainingFireTicks(0);

            if (!player.hasEffect(MobEffects.LEVITATION)) {
                world.sendParticles(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1), x, y, z, 50, 0.1, 0.1, 0.1, 0.05);
                action.cancel();
                return;
            }

            world.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0);
        }, 1);
    }

    private void onJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        lobbyManager.sendToLobby(handler.player);
    }

    private boolean isLobby(Level world) {
        return lobbyManager.getLobbyWorld() == world;
    }
}
