package work.lclpnet.lobby.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;

import java.util.function.Predicate;

public class RayCaster {

    private RayCaster() {}

    public static BlockHitResult rayCastBlockCollision(BlockGetter blockView, GenericRaycastContext ctx) {
        return BlockGetter.traverseBlocks(ctx.start(), ctx.end(), ctx, (context, pos) -> {
            BlockState blockState = blockView.getBlockState(pos);
            FluidState fluidState = blockView.getFluidState(pos);

            Vec3 start = context.start();
            Vec3 end = context.end();

            VoxelShape blockShape = context.getBlockShape(blockState, blockView, pos);

            BlockHitResult blockHit = blockView.clipWithInteractionOverride(start, end, pos, blockShape, blockState);

            VoxelShape fluidShape = context.getFluidShape(fluidState, blockView, pos);
            BlockHitResult fluidHit = fluidShape.clip(start, end, pos);

            double blockDistance = blockHit == null ? Double.MAX_VALUE : start.distanceToSqr(blockHit.getLocation());
            double fluidDistance = fluidHit == null ? Double.MAX_VALUE : start.distanceToSqr(fluidHit.getLocation());

            return blockDistance <= fluidDistance ? blockHit : fluidHit;
        }, context -> {
            Vec3 end = context.end();
            Vec3 dir = context.start().subtract(end);

            return BlockHitResult.miss(end, Direction.getApproximateNearest(dir.x, dir.y, dir.z), BlockPos.containing(end));
        });
    }

    public static BlockHitResult rayCast(Vec3 start, Vec3 end, Predicate<BlockPos> predicate) {
        return BlockGetter.traverseBlocks(start, end, null, (o, pos) -> {
            if (predicate.test(pos)) {
                Vec3 dir = start.subtract(end);
                return new BlockHitResult(end, Direction.getApproximateNearest(dir.x, dir.y, dir.z), BlockPos.containing(end), false);
            }

            return null;
        }, o -> {
            Vec3 dir = start.subtract(end);
            return BlockHitResult.miss(end, Direction.getApproximateNearest(dir.x, dir.y, dir.z), BlockPos.containing(end));
        });
    }

    public record GenericRaycastContext(Vec3 start, Vec3 end, ClipContext.Block shapeType, ClipContext.Fluid fluidHandling) {

        public VoxelShape getBlockShape(BlockState state, BlockGetter world, BlockPos pos) {
            return this.shapeType.get(state, world, pos, CollisionContext.empty());
        }

        public VoxelShape getFluidShape(FluidState state, BlockGetter world, BlockPos pos) {
            return this.fluidHandling.canPick(state) ? state.getShape(world, pos) : Shapes.empty();
        }
    }
}
