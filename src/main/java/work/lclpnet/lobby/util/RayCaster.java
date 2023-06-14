package work.lclpnet.lobby.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;

import java.util.function.Predicate;

public class RayCaster {

    private RayCaster() {}

    public static BlockHitResult rayCastBlockCollision(BlockView blockView, GenericRaycastContext ctx) {
        return BlockView.raycast(ctx.start(), ctx.end(), ctx, (context, pos) -> {
            BlockState blockState = blockView.getBlockState(pos);
            FluidState fluidState = blockView.getFluidState(pos);

            Vec3d start = context.start();
            Vec3d end = context.end();

            VoxelShape blockShape = context.getBlockShape(blockState, blockView, pos);

            BlockHitResult blockHit = blockView.raycastBlock(start, end, pos, blockShape, blockState);

            VoxelShape fluidShape = context.getFluidShape(fluidState, blockView, pos);
            BlockHitResult fluidHit = fluidShape.raycast(start, end, pos);

            double blockDistance = blockHit == null ? Double.MAX_VALUE : start.squaredDistanceTo(blockHit.getPos());
            double fluidDistance = fluidHit == null ? Double.MAX_VALUE : start.squaredDistanceTo(fluidHit.getPos());

            return blockDistance <= fluidDistance ? blockHit : fluidHit;
        }, context -> {
            Vec3d end = context.end();
            Vec3d dir = context.start().subtract(end);

            return BlockHitResult.createMissed(end, Direction.getFacing(dir.x, dir.y, dir.z), BlockPos.ofFloored(end));
        });
    }

    public static BlockHitResult rayCast(Vec3d start, Vec3d end, Predicate<BlockPos> predicate) {
        return BlockView.raycast(start, end, null, (o, pos) -> {
            if (predicate.test(pos)) {
                Vec3d dir = start.subtract(end);
                return new BlockHitResult(end, Direction.getFacing(dir.x, dir.y, dir.z), BlockPos.ofFloored(end), false);
            }

            return null;
        }, o -> {
            Vec3d dir = start.subtract(end);
            return BlockHitResult.createMissed(end, Direction.getFacing(dir.x, dir.y, dir.z), BlockPos.ofFloored(end));
        });
    }

    public record GenericRaycastContext(Vec3d start, Vec3d end, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling) {

        public VoxelShape getBlockShape(BlockState state, BlockView world, BlockPos pos) {
            return this.shapeType.get(state, world, pos, ShapeContext.absent());
        }

        public VoxelShape getFluidShape(FluidState state, BlockView world, BlockPos pos) {
            return this.fluidHandling.handled(state) ? state.getShape(world, pos) : VoxelShapes.empty();
        }
    }
}
