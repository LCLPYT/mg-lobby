package work.lclpnet.lobby.decor.seat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import work.lclpnet.kibu.hook.entity.PlayerInteractionHooks;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.hook.player.PlayerMountHooks;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.lobby.di.ActivityScope;
import work.lclpnet.lobby.util.WorldModifier;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ActivityScope
public class SeatHandler {

    private final WorldModifier worldModifier;
    private final SeatProvider seatProvider;
    private final Map<UUID, Vec3d> positions = new HashMap<>();
    private final HookRegistrar hookRegistrar;

    @Inject
    public SeatHandler(WorldModifier worldModifier, SeatProvider seatProvider, HookRegistrar hookRegistrar) {
        this.seatProvider = seatProvider;
        this.worldModifier = worldModifier;
        this.hookRegistrar = hookRegistrar;
    }

    public void init() {
        hookRegistrar.registerHook(PlayerInteractionHooks.USE_BLOCK, this::onRightClickBlock);
        hookRegistrar.registerHook(PlayerConnectionHooks.QUIT, ServerPlayerEntity::stopRiding);
        hookRegistrar.registerHook(PlayerMountHooks.DISMOUNTED, this::onDismount);
    }

    private ActionResult onRightClickBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

        BlockPos pos = hitResult.getBlockPos();

        if (sit(serverPlayer, world, pos)) {
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private void onDismount(ServerPlayerEntity player, Entity vehicle) {
        if (!vehicle.getCommandTags().contains("seat")) return;

        PlayerSeatCallback.AFTER_GET_UP.invoker().onGottenUp(player);

        vehicle.discard();

        Vec3d prev = positions.remove(player.getUuid());
        if (prev == null) return;

        player.teleport(player.getServerWorld(), prev.getX(), prev.getY(), prev.getZ(), Set.of(), player.getYaw(), player.getPitch());
    }

    protected boolean sit(ServerPlayerEntity player, World world, BlockPos pos) {
        if (player.isSneaking() || player.isSpectator()) return false;

        Entity seatEntity = seatProvider.getSeat(world, pos);

        if (seatEntity == null || PlayerSeatCallback.BEFORE_SIT.invoker().onSeat(player, pos)) return false;

        worldModifier.spawnEntity(seatEntity);

        positions.putIfAbsent(player.getUuid(), player.getPos());

        player.startRiding(seatEntity, true);

        PlayerSeatCallback.AFTER_SIT.invoker().onSeated(player, pos);

        return true;
    }
}
