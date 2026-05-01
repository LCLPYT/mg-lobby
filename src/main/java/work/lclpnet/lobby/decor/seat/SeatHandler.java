package work.lclpnet.lobby.decor.seat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.entity.PlayerInteractionHooks;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.hook.player.PlayerMountHooks;
import work.lclpnet.lobby.di.ActivityScope;
import work.lclpnet.lobby.util.WorldModifier;

import javax.inject.Inject;
import java.util.*;

@ActivityScope
public class SeatHandler {

    private final WorldModifier worldModifier;
    private final SeatProvider seatProvider;
    private final Map<UUID, Vec3> positions = new HashMap<>();
    private final Set<UUID> changedSeat = new HashSet<>();
    private final HookRegistrar hookRegistrar;

    @Inject
    public SeatHandler(WorldModifier worldModifier, SeatProvider seatProvider, HookRegistrar hookRegistrar) {
        this.seatProvider = seatProvider;
        this.worldModifier = worldModifier;
        this.hookRegistrar = hookRegistrar;
    }

    public void init() {
        hookRegistrar.registerHook(PlayerInteractionHooks.USE_BLOCK, this::onRightClickBlock);
        hookRegistrar.registerHook(PlayerConnectionHooks.QUIT, ServerPlayer::stopRiding);
        hookRegistrar.registerHook(PlayerMountHooks.DISMOUNTED, this::onDismount);
    }

    private InteractionResult onRightClickBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        BlockPos pos = hitResult.getBlockPos();

        if (sit(serverPlayer, world, pos)) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void onDismount(ServerPlayer player, Entity vehicle) {
        if (!vehicle.entityTags().contains("seat")) return;

        PlayerSeatCallback.AFTER_GET_UP.invoker().onGottenUp(player);

        vehicle.discard();

        UUID uuid = player.getUUID();

        if (changedSeat.remove(uuid)) return;

        Vec3 prev = positions.remove(uuid);
        if (prev == null) return;

        player.teleportTo(player.level(), prev.x(), prev.y(), prev.z(), Set.of(), player.getYRot(), player.getXRot(), true);
    }

    protected boolean sit(ServerPlayer player, Level world, BlockPos pos) {
        if (player.isShiftKeyDown() || player.isSpectator()) return false;

        Entity seatEntity = seatProvider.getSeat(world, pos);

        if (seatEntity == null || PlayerSeatCallback.BEFORE_SIT.invoker().onSeat(player, pos)) return false;

        worldModifier.spawnEntity(seatEntity);

        UUID uuid = player.getUUID();
        positions.putIfAbsent(uuid, player.position());

        if (player.getVehicle() != null) {
            changedSeat.add(uuid);
        }

        player.startRiding(seatEntity, true, false);

        PlayerSeatCallback.AFTER_SIT.invoker().onSeated(player, pos);

        return true;
    }
}
