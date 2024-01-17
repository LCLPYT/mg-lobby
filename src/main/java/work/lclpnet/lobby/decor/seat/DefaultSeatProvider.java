package work.lclpnet.lobby.decor.seat;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import work.lclpnet.kibu.access.entity.ArmorStandAccess;

import javax.annotation.Nullable;

public class DefaultSeatProvider implements SeatProvider {

    private DefaultSeatProvider() {}

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

        if (isObstructed(pos, world)) {
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

    private boolean isObstructed(BlockPos pos, BlockView view) {
        if (hasCollision(view, pos.up())) return true;

        // check if chair is sunken into the ground
        return isFullBlock(view, pos.north()) &&
                isFullBlock(view, pos.south()) &&
                isFullBlock(view, pos.west()) &&
                isFullBlock(view, pos.east()) &&
                isFullBlock(view, pos.add(1, 0, 1)) &&
                isFullBlock(view, pos.add(-1, 0, -1)) &&
                isFullBlock(view, pos.add(1, 0, -1)) &&
                isFullBlock(view, pos.add(-1, 0, 1));
    }

    private boolean hasCollision(BlockView view, BlockPos pos) {
        return !view.getBlockState(pos).getCollisionShape(view, pos).isEmpty();
    }

    private boolean isFullBlock(BlockView view, BlockPos pos) {
        VoxelShape shape = view.getBlockState(pos).getCollisionShape(view, pos);
        if (shape.isEmpty()) return false;

        Box box = shape.getBoundingBox();
        return box.getLengthY() >= 1 && box.getLengthX() >= 1 && box.getLengthZ() >= 1;
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

    public static DefaultSeatProvider getInstance() {
        return Holder.INSTANCE;
    }

    private static final class Holder {
        private static final DefaultSeatProvider INSTANCE = new DefaultSeatProvider();
    }
}
