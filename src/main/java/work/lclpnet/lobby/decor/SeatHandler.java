package work.lclpnet.lobby.decor;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import work.lclpnet.kibu.access.entity.ArmorStandAccess;
import work.lclpnet.kibu.hook.entity.PlayerInteractionHooks;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.hook.player.PlayerMountHooks;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.lobby.util.WorldModifier;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SeatHandler {

    private final WorldModifier worldModifier;
    private final SeatProvider seatProvider;
    private final Map<UUID, Vec3d> positions = new HashMap<>();

    public SeatHandler(WorldModifier worldModifier) {
        this(worldModifier, getDefaultSeatProvider());
    }

    public SeatHandler(WorldModifier worldModifier, SeatProvider seatProvider) {
        this.seatProvider = seatProvider;
        this.worldModifier = worldModifier;
    }

    public void init(HookRegistrar hookRegistrar) {
        hookRegistrar.registerHook(PlayerInteractionHooks.USE_BLOCK, this::onRightClickBlock);
        hookRegistrar.registerHook(PlayerConnectionHooks.QUIT, ServerPlayerEntity::stopRiding);
        hookRegistrar.registerHook(PlayerMountHooks.DISMOUNTED, this::onDismount);
    }

    private ActionResult onRightClickBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();

        if (sit(player, world, pos)) {
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private void onDismount(ServerPlayerEntity player, Entity vehicle) {
        if (vehicle == null || !vehicle.getCommandTags().contains("seat")) return;

        vehicle.discard();

        Vec3d prev = positions.remove(player.getUuid());
        if (prev == null) return;

        player.teleport(player.getServerWorld(), prev.getX(), prev.getY(), prev.getZ(), Set.of(), player.getYaw(), player.getPitch());
    }

    protected boolean sit(PlayerEntity player, World world, BlockPos pos) {
        if (player.isSneaking() || player.isSpectator()) return false;

        Entity seatEntity = seatProvider.getSeat(world, pos);

        if (seatEntity == null) return false;

        worldModifier.spawnEntity(seatEntity);

        positions.putIfAbsent(player.getUuid(), player.getPos());

        player.startRiding(seatEntity, true);

        return true;
    }

    public static SeatProvider getDefaultSeatProvider() {
        return DefaultProviderHolder.DEFAULT_SEAT_PROVIDER;
    }

    public interface SeatProvider {
        @Nullable
        Entity getSeat(World world, BlockPos pos);
    }

    // wrap default seat provider inside nested class to make it lazy loadable + singleton
    private static final class DefaultProviderHolder {
        private static final SeatProvider DEFAULT_SEAT_PROVIDER = new SeatProvider() {
            @Nullable
            @Override
            public Entity getSeat(World world, BlockPos pos) {
                BlockState state = world.getBlockState(pos);

                if (!state.isIn(BlockTags.STAIRS)) {
                    // not a seat
                    return null;
                }

                if (state.getProperties().contains(Properties.BLOCK_HALF) && state.get(Properties.BLOCK_HALF) != BlockHalf.BOTTOM) {
                    // chair is upside down
                    return null;
                }

                final Vec3d seatPos = getSeatPosition(state, pos);

                var blockingEntities = world.getEntitiesByClass(ArmorStandEntity.class, Box.of(seatPos, 1, 1, 1), entity -> {
                    if (!entity.getCommandTags().contains("seat")) return false;

                    if (entity.hasPassengers()) return true;

                    // there somehow is an unused seat, remove it
                    entity.discard();

                    return false;
                });

                if (!blockingEntities.isEmpty()) {
                    // seat is blocked
                    return null;
                }

                final float yaw = getYaw(state);

                ArmorStandEntity stand = new ArmorStandEntity(world, seatPos.getX(), seatPos.getY(), seatPos.getZ());
                stand.setInvisible(true);
                stand.setInvulnerable(true);
                stand.setNoGravity(true);
                stand.setYaw(yaw);
                stand.addCommandTag("seat");
                ArmorStandAccess.setSmall(stand, true);
                ArmorStandAccess.setMarker(stand, true);

                return stand;
            }

            private Vec3d getSeatPosition(BlockState state, BlockPos pos) {
                final int x = pos.getX(), y = pos.getY(), z = pos.getZ();

                final var properties = state.getProperties();

                if (!properties.contains(Properties.HORIZONTAL_FACING)) {
                    return new Vec3d(x + 0.5, y + 0.25, z + 0.5);
                }

                final Direction direction = state.get(Properties.HORIZONTAL_FACING);
                final StairShape shape;

                if (!properties.contains(Properties.STAIR_SHAPE) || (shape = state.get(Properties.STAIR_SHAPE)) == StairShape.STRAIGHT) {
                    return switch (direction) {
                        case NORTH -> new Vec3d(x + 0.5, y + 0.25, z + 0.75);
                        case SOUTH -> new Vec3d(x + 0.5, y + 0.25, z + 0.25);
                        case WEST -> new Vec3d(x + 0.75, y + 0.25, z + 0.5);
                        case EAST -> new Vec3d(x + 0.25, y + 0.25, z + 0.5);
                        default -> new Vec3d(x + 0.5, y + 0.25, z + 0.5);  // unreachable
                    };
                }

                if (shape == StairShape.OUTER_RIGHT || shape == StairShape.INNER_RIGHT) {
                    return switch (direction) {
                        case NORTH -> new Vec3d(x + 0.25, y + 0.25, z + 0.75);
                        case SOUTH -> new Vec3d(x + 0.75, y + 0.25, z + 0.25);
                        case WEST -> new Vec3d(x + 0.75, y + 0.25, z + 0.75);
                        case EAST -> new Vec3d(x + 0.25, y + 0.25, z + 0.25);
                        default -> new Vec3d(x + 0.5, y + 0.25, z + 0.5);  // unreachable
                    };
                }

                // shape is OUTER_LEFT or INNER_LEFT
                return switch (direction) {
                    case NORTH -> new Vec3d(x + 0.75, y + 0.25, z + 0.75);
                    case SOUTH -> new Vec3d(x + 0.25, y + 0.25, z + 0.25);
                    case WEST -> new Vec3d(x + 0.75, y + 0.25, z + 0.25);
                    case EAST -> new Vec3d(x + 0.25, y + 0.25, z + 0.75);
                    default -> new Vec3d(x + 0.5, y + 0.25, z + 0.5);  // unreachable
                };
            }

            private float getYaw(BlockState state) {
                final var properties = state.getProperties();

                if (!properties.contains(Properties.HORIZONTAL_FACING)) return 0;

                final Direction direction = state.get(Properties.HORIZONTAL_FACING);
                final StairShape shape;

                if (!properties.contains(Properties.STAIR_SHAPE) || (shape = state.get(Properties.STAIR_SHAPE)) == StairShape.STRAIGHT) {
                    return switch (direction) {
                        case NORTH -> 0;
                        case SOUTH -> 180;
                        case WEST -> -90;
                        case EAST -> 90;
                        default -> 0;  // unreachable
                    };
                }

                if (shape == StairShape.OUTER_RIGHT || shape == StairShape.INNER_RIGHT) {
                    return switch (direction) {
                        case NORTH -> 45;
                        case SOUTH -> -135;
                        case WEST -> -45;
                        case EAST -> 135;
                        default -> 0;  // unreachable
                    };
                }

                // shape is OUTER_LEFT or INNER_LEFT
                return switch (direction) {
                    case NORTH -> -45;
                    case SOUTH -> 135;
                    case WEST -> -135;
                    case EAST -> 45;
                    default -> 0;  // unreachable
                };
            }
        };
    }
}
