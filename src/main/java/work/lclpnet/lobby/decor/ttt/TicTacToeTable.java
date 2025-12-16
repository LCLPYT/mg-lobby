package work.lclpnet.lobby.decor.ttt;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class TicTacToeTable implements Pair<BlockPos, BlockPos> {

    private final BlockPos left, right;
    private final BlockPos center;
    private ServerPlayer leftPlayer, rightPlayer;

    public TicTacToeTable(Pair<BlockPos, BlockPos> pair) {
        this(pair.left(), pair.right());
    }

    public TicTacToeTable(BlockPos left, BlockPos right) {
        Vec3i dir = right.subtract(left);
        if (left.offset(Math.abs(dir.getX()), Math.abs(dir.getY()), Math.abs(dir.getZ())).equals(right)) {
            this.left = left;
            this.right = right;
        } else {
            this.left = right;
            this.right = left;
        }

        this.center = calcCenter(left, right);
    }

    protected BlockPos calcCenter(BlockPos left, BlockPos right) {
        BlockPos diff = left.subtract(right);

        // check distance is 2
        if (Math.abs(diff.distSqr(Vec3i.ZERO) - 4) > 1e-9) {
            throw new IllegalArgumentException("Seats must be exactly 2 blocks apart");
        }

        return right.offset(diff.getX() / 2, diff.getY() / 2, diff.getZ() / 2);
    }

    @Override
    public BlockPos left() {
        return left;
    }

    @Override
    public BlockPos right() {
        return right;
    }

    @Nullable
    public ServerPlayer player(int i) {
        if (i == 0) return leftPlayer;
        if (i == 1) return rightPlayer;

        return null;
    }

    public void player(int i, ServerPlayer player) {
        if (i == 0) leftPlayer = player;
        else if (i == 1) rightPlayer = player;
    }

    public int playerIndex(ServerPlayer player) {
        if (player == null) return -1;
        if (player == leftPlayer) return 0;
        if (player == rightPlayer) return 1;

        return -1;
    }

    public int playerIndex(BlockPos pos) {
        if (pos == null) return -1;
        if (left.equals(pos)) return 0;
        if (right.equals(pos)) return 1;

        return -1;
    }

    public ServerPlayer opponent(int i) {
        if (i == -1) return null;

        return player(1 - i);
    }

    public boolean full() {
        return leftPlayer != null && rightPlayer != null;
    }

    public Collection<ServerPlayer> players() {
        var players = new ArrayList<ServerPlayer>();

        if (leftPlayer != null) players.add(leftPlayer);
        if (rightPlayer != null) players.add(rightPlayer);

        return players;
    }

    public BlockPos center() {
        return center;
    }

    public Vec3 direction() {
        return Vec3.atLowerCornerOf(center.subtract(left));
    }

    public void clear() {
        leftPlayer = null;
        rightPlayer = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicTacToeTable table = (TicTacToeTable) o;
        return Objects.equals(left, table.left) && Objects.equals(right, table.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
