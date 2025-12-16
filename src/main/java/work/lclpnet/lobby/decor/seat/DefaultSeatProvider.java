package work.lclpnet.lobby.decor.seat;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.access.entity.ArmorStandAccess;

public class DefaultSeatProvider implements SeatProvider {

    private static final float OFFSET_Y = 0.5f;

    private DefaultSeatProvider() {}

    @Nullable
    @Override
    public Entity getSeat(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (!state.is(BlockTags.STAIRS)) {
            // not a seat
            return null;
        }

        if (state.getProperties().contains(BlockStateProperties.HALF) && state.getValue(BlockStateProperties.HALF) != Half.BOTTOM) {
            // chair is upside down
            return null;
        }

        if (isObstructed(pos, world)) {
            return null;
        }

        final Vec3 seatPos = getSeatPosition(state, pos);

        var blockingEntities = world.getEntitiesOfClass(ArmorStand.class, AABB.ofSize(seatPos, 1, 1, 1), entity -> {
            if (!entity.getTags().contains("seat")) return false;

            if (entity.isVehicle()) return true;

            // there somehow is an unused seat, remove it
            entity.discard();

            return false;
        });

        if (!blockingEntities.isEmpty()) {
            // seat is blocked
            return null;
        }

        final float yaw = getYaw(state);

        ArmorStand stand = new ArmorStand(world, seatPos.x(), seatPos.y(), seatPos.z());
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setNoGravity(true);
        stand.setYRot(yaw);
        stand.addTag("seat");
        ArmorStandAccess.setSmall(stand, true);
        ArmorStandAccess.setMarker(stand, true);

        return stand;
    }

    private boolean isObstructed(BlockPos pos, BlockGetter view) {
        if (hasCollision(view, pos.above())) return true;

        // check if chair is sunken into the ground
        return isFullBlock(view, pos.north()) &&
                isFullBlock(view, pos.south()) &&
                isFullBlock(view, pos.west()) &&
                isFullBlock(view, pos.east()) &&
                isFullBlock(view, pos.offset(1, 0, 1)) &&
                isFullBlock(view, pos.offset(-1, 0, -1)) &&
                isFullBlock(view, pos.offset(1, 0, -1)) &&
                isFullBlock(view, pos.offset(-1, 0, 1));
    }

    private boolean hasCollision(BlockGetter view, BlockPos pos) {
        return !view.getBlockState(pos).getCollisionShape(view, pos).isEmpty();
    }

    private boolean isFullBlock(BlockGetter view, BlockPos pos) {
        VoxelShape shape = view.getBlockState(pos).getCollisionShape(view, pos);
        if (shape.isEmpty()) return false;

        AABB box = shape.bounds();
        return box.getYsize() >= 1 && box.getXsize() >= 1 && box.getZsize() >= 1;
    }

    private Vec3 getSeatPosition(BlockState state, BlockPos pos) {
        final int x = pos.getX(), y = pos.getY(), z = pos.getZ();

        final var properties = state.getProperties();

        if (!properties.contains(BlockStateProperties.HORIZONTAL_FACING)) {
            return new Vec3(x + 0.5, y + OFFSET_Y, z + 0.5);
        }

        final Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        final StairsShape shape;

        if (!properties.contains(BlockStateProperties.STAIRS_SHAPE) || (shape = state.getValue(BlockStateProperties.STAIRS_SHAPE)) == StairsShape.STRAIGHT) {
            return switch (direction) {
                case NORTH -> new Vec3(x + 0.5, y + OFFSET_Y, z + 0.75);
                case SOUTH -> new Vec3(x + 0.5, y + OFFSET_Y, z + 0.25);
                case WEST -> new Vec3(x + 0.75, y + OFFSET_Y, z + 0.5);
                case EAST -> new Vec3(x + 0.25, y + OFFSET_Y, z + 0.5);
                default -> new Vec3(x + 0.5, y + OFFSET_Y, z + 0.5);  // unreachable
            };
        }

        if (shape == StairsShape.OUTER_RIGHT || shape == StairsShape.INNER_RIGHT) {
            return switch (direction) {
                case NORTH -> new Vec3(x + 0.25, y + OFFSET_Y, z + 0.75);
                case SOUTH -> new Vec3(x + 0.75, y + OFFSET_Y, z + 0.25);
                case WEST -> new Vec3(x + 0.75, y + OFFSET_Y, z + 0.75);
                case EAST -> new Vec3(x + 0.25, y + OFFSET_Y, z + 0.25);
                default -> new Vec3(x + 0.5, y + OFFSET_Y, z + 0.5);  // unreachable
            };
        }

        // shape is OUTER_LEFT or INNER_LEFT
        return switch (direction) {
            case NORTH -> new Vec3(x + 0.75, y + OFFSET_Y, z + 0.75);
            case SOUTH -> new Vec3(x + 0.25, y + OFFSET_Y, z + 0.25);
            case WEST -> new Vec3(x + 0.75, y + OFFSET_Y, z + 0.25);
            case EAST -> new Vec3(x + 0.25, y + OFFSET_Y, z + 0.75);
            default -> new Vec3(x + 0.5, y + OFFSET_Y, z + 0.5);  // unreachable
        };
    }

    private float getYaw(BlockState state) {
        final var properties = state.getProperties();

        if (!properties.contains(BlockStateProperties.HORIZONTAL_FACING)) return 0;

        final Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        final StairsShape shape;

        if (!properties.contains(BlockStateProperties.STAIRS_SHAPE) || (shape = state.getValue(BlockStateProperties.STAIRS_SHAPE)) == StairsShape.STRAIGHT) {
            return switch (direction) {
                case NORTH -> 0;
                case SOUTH -> 180;
                case WEST -> -90;
                case EAST -> 90;
                default -> 0;  // unreachable
            };
        }

        if (shape == StairsShape.OUTER_RIGHT || shape == StairsShape.INNER_RIGHT) {
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
