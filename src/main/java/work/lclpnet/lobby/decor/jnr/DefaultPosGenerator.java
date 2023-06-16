package work.lclpnet.lobby.decor.jnr;

import it.unimi.dsi.fastutil.ints.IntFloatPair;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import work.lclpnet.lobby.util.RayCaster;

import javax.annotation.Nullable;
import java.util.*;

public class DefaultPosGenerator implements PosGenerator {

    private final Random random = new Random();
    private final List<Vec3i> moves = new ArrayList<>();
    private final ServerWorld world;
    private final List<IntFloatPair> stagnation;
    private final Stack<BlockPos> history;
    private final int stagnationY;
    private final int longJumpY;
    private BlockPos from;
    private boolean lastMoveUp = false;

    public DefaultPosGenerator(ServerWorld world, Stack<BlockPos> history, Config config) {
        initPossibleMoves();

        if (history.size() == 0) {
            throw new IllegalArgumentException("History must have a start element");
        }

        this.history = history;
        this.world = world;

        BlockPos from = history.peek();

        final int fromY = from.getY();

        stagnationY = fromY + config.stagnationOffset();
        longJumpY = fromY + config.longJumpOffset();

        stagnation = config.stagnation().stream()
                .map(pair -> IntFloatPair.of(fromY + pair.leftInt(), pair.rightFloat()))
                .sorted(Comparator.comparingInt(IntFloatPair::leftInt))
                .toList();
    }

    private void initPossibleMoves() {
        //Orange
        pos(-2, 0, 3);
        pos(-1, 0, 3);
        pos(0, 0, 3);
        pos(1, 0, 3);
        pos(2, 0, 3);

        pos(-2, 0, -3);
        pos(-1, 0, -3);
        pos(0, 0, -3);
        pos(1, 0, -3);
        pos(2, 0, -3);

        pos(3, 0, -2);
        pos(3, 0, -1);
        pos(3, 0, 0);
        pos(3, 0, 1);
        pos(3, 0, 2);

        pos(-3, 0, -2);
        pos(-3, 0, -1);
        pos(-3, 0, 0);
        pos(-3, 0, 1);
        pos(-3, 0, 2);

        pos(2, 0, 2);
        pos(-2, 0, -2);
        pos(2, 0, -2);
        pos(-2, 0, 2);

        //Red
        pos(-2, 0, 4);
        pos(-1, 0, 4);
        pos(0, 0, 4);
        pos(1, 0, 4);
        pos(2, 0, 4);

        pos(-2, 0, -4);
        pos(-1, 0, -4);
        pos(0, 0, -4);
        pos(1, 0, -4);
        pos(2, 0, -4);

        pos(4, 0, -2);
        pos(4, 0, -1);
        pos(4, 0, 0);
        pos(4, 0, 1);
        pos(4, 0, 2);

        pos(-4, 0, -2);
        pos(-4, 0, -1);
        pos(-4, 0, 0);
        pos(-4, 0, 1);
        pos(-4, 0, 2);

        pos(3, 0, 3);
        pos(-3, 0, -3);
        pos(3, 0, -3);
        pos(-3, 0, 3);

        //Purple
        pos(4, 0, 3);
        pos(-4, 0, -3);
        pos(4, 0, -3);
        pos(-4, 0, 3);

        pos(3, 0, 4);
        pos(-3, 0, -4);
        pos(3, 0, -4);
        pos(-3, 0, 4);

        //Light yellow
        pos(-1, 1, 2);
        pos(0, 1, 2);
        pos(1, 1, 2);

        pos(-1, 1, -2);
        pos(0, 1, -2);
        pos(1, 1, -2);

        pos(2, 1, -1);
        pos(2, 1, 0);
        pos(2, 1, 1);

        pos(-2, 1, -1);
        pos(-2, 1, 0);
        pos(-2, 1, 1);

        //Light Orange
        pos(-2, 1, 3);
        pos(-1, 1, 3);
        pos(0, 1, 3);
        pos(1, 1, 3);
        pos(2, 1, 3);

        pos(-2, 1, -3);
        pos(-1, 1, -3);
        pos(0, 1, -3);
        pos(1, 1, -3);
        pos(2, 1, -3);

        pos(3, 1, -2);
        pos(3, 1, -1);
        pos(3, 1, 0);
        pos(3, 1, 1);
        pos(3, 1, 2);

        pos(-3, 1, -2);
        pos(-3, 1, -1);
        pos(-3, 1, 0);
        pos(-3, 1, 1);
        pos(-3, 1, 2);

        pos(2, 1, 2);
        pos(-2, 1, -2);
        pos(2, 1, -2);
        pos(-2, 1, 2);

        //Light Red
        pos(-2, 1, 4);
        pos(-1, 1, 4);
        pos(0, 1, 4);
        pos(1, 1, 4);
        pos(2, 1, 4);

        pos(-2, 1, -4);
        pos(-1, 1, -4);
        pos(0, 1, -4);
        pos(1, 1, -4);
        pos(2, 1, -4);

        pos(4, 1, -2);
        pos(4, 1, -1);
        pos(4, 1, 0);
        pos(4, 1, 1);
        pos(4, 1, 2);

        pos(-4, 1, -2);
        pos(-4, 1, -1);
        pos(-4, 1, 0);
        pos(-4, 1, 1);
        pos(-4, 1, 2);

        pos(3, 1, 3);
        pos(-3, 1, -3);
        pos(3, 1, -3);
        pos(-3, 1, 3);
    }

    private void pos(int dx, int dy, int dz) {
        moves.add(new Vec3i(dx, dy, dz));
    }

    private List<BlockPos> getPossibleMoves() {
        List<BlockPos> possible = new ArrayList<>();

        for (Vec3i move : moves) {
            if (bias(move)) continue;

            BlockPos next = from.add(move);

            if (!isMovePossible(next)) continue;

            possible.add(next);
        }

        return possible;
    }

    private boolean bias(Vec3i move) {
        // prevent height stagnation below configured height
        if (from.getY() < stagnationY && move.getY() < 1) return true;

        // disallow successive stagnation
        if (move.getY() < 1 && !lastMoveUp) return true;

        // prevent long jumps below configured height
        if (from.getY() < longJumpY && (Math.abs(move.getX()) > 3 || Math.abs(move.getZ()) > 3)) return true;

        if (move.getY() >= 1) return false;

        // decide possible stagnation, based of configured values
        for (var pair : stagnation) {
            if (from.getY() < pair.leftInt() && random.nextFloat() < pair.rightFloat()) {
                return true;
            }
        }

        return false;
    }

    private boolean isMovePossible(BlockPos next) {
        return world.getBlockState(next).isAir() && spaceAbove(next) && !isAboveOther(next) && !isWayBlocked(next) && !isBlockingWay(next);
    }

    private boolean isBlockingWay(BlockPos pos) {
        ListIterator<BlockPos> iterator = history.listIterator(history.size());

        final int y = pos.getY();

        while (iterator.hasPrevious()) {
            BlockPos prev = iterator.previous();
            if (y - prev.getY() > 3) break;  // too far down the history => as the history is continuous, we stop

            int nextIdx = iterator.nextIndex();
            if (nextIdx >= history.size()) continue;

            BlockPos next = history.get(nextIdx);

            // check if placing a block at pos would block the way from prev to next

            for (int i = 1; i < 3; i++) {
                if (isBetween(from.up(i), next.up(i), pos)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isWayBlocked(BlockPos next) {
        for (int i = 1; i < 3; i++) {
            if (blocksBetween(from.up(i), next.up(i))) {
                return true;
            }
        }

        return false;
    }

    private boolean blocksBetween(BlockPos a, BlockPos b) {
        Vec3d start = a.toCenterPos();
        Vec3d end = b.toCenterPos();

        return blocksBetween(start, end);
    }

    private static boolean isBetween(BlockPos a, BlockPos b, BlockPos c) {
        Vec3d start = a.toCenterPos();
        Vec3d end = b.toCenterPos();

        return isBetween(start, end, c);
    }

    private static boolean isBetween(Vec3d start, Vec3d end, BlockPos position) {
        BlockHitResult result = RayCaster.rayCast(start, end, pos -> pos.equals(position));
        return result.getType() == HitResult.Type.BLOCK;
    }

    private boolean blocksBetween(Vec3d start, Vec3d end) {
        var ctx = new RayCaster.GenericRaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY);

        BlockHitResult result = RayCaster.rayCastBlockCollision(world, ctx);

        return result.getType() == HitResult.Type.BLOCK;
    }

    /**
     * Checks, if there are at least three blocks free above other nodes in history.
     * @param pos The position.
     * @return True, if the given position is blocking another node in history.
     */
    private boolean isAboveOther(BlockPos pos) {
        ListIterator<BlockPos> iterator = history.listIterator(history.size());

        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        while (iterator.hasPrevious()) {
            BlockPos prev = iterator.previous();
            if (y - prev.getY() > 3) break;  // too far down the history => as the history is continuous, we stop

            if (prev.getX() == x && prev.getZ() == z) {
                return true;
            }
        }

        return false;
    }

    private boolean spaceAbove(BlockPos pos) {
        for (int i = 1; i < 3; i++) {
            BlockPos check = pos.up(i);
            BlockState state = world.getBlockState(check);

            if (!state.getCollisionShape(world, check).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    @Override
    public BlockPos generate() {
        from = history.peek();  // update from position, as the consumer could have changed the history

        var moves = getPossibleMoves();

        if (moves.isEmpty()) return null;  // no moves possible

        BlockPos next = moves.get(random.nextInt(moves.size()));
        lastMoveUp = next.getY() - from.getY() >= 1;

        from = next;

        return next;
    }

    @Override
    public void reset() {
        this.lastMoveUp = false;
        this.from = history.get(0);
    }

    /**
     * A configuration for the {@link DefaultPosGenerator}.
     * @param stagnationOffset The height offset at which the positions can occasionally stay on the same height.
     * @param longJumpOffset The height offset at which long jumps can occur.
     * @param stagnation A list of different stagnation values. Every pair designates a height and a probability of how
     *                   likely stagnation is under the given height.
     */
    public record Config(int stagnationOffset, int longJumpOffset, List<IntFloatPair> stagnation) {}
}
