package work.lclpnet.lobby.event;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
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
import work.lclpnet.lobby.api.LobbyManager;

import javax.inject.Inject;
import java.util.Set;

public class LobbyListener implements HookListenerModule {

    private final LobbyManager lobbyManager;

    @Inject
    public LobbyListener(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
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

        if (isLobby(world) && to.getY() < world.getBottomY()) {
            // teleport player back to spawn location
            Vec3d spawn = lobbyManager.getLobbySpawn();

            player.teleport(world, spawn.getX(), spawn.getY(), spawn.getZ(), Set.of(), 0, 0);
        }

        return false;  // false = allow movement
    }

    private void onJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        lobbyManager.sendToLobby(handler.player);
    }

    private boolean isLobby(World world) {
        return lobbyManager.getLobbyWorld() == world;
    }
}
