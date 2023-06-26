package work.lclpnet.lobby.decor.ttt;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class TicTacToeTable implements Pair<BlockPos, BlockPos> {

    private final BlockPos left, right;
    private final BlockPos center;
    private ServerPlayerEntity leftPlayer, rightPlayer;

    public TicTacToeTable(Pair<BlockPos, BlockPos> pair) {
        this(pair.left(), pair.right());
    }

    public TicTacToeTable(BlockPos left, BlockPos right) {
        Vec3i dir = right.subtract(left);
        if (left.add(Math.abs(dir.getX()), Math.abs(dir.getY()), Math.abs(dir.getZ())).equals(right)) {
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
        if (Math.abs(diff.getSquaredDistance(Vec3i.ZERO) - 4) > 1e-9) {
            throw new IllegalArgumentException("Seats must be exactly 2 blocks apart");
        }

        return right.add(diff.getX() / 2, diff.getY() / 2, diff.getZ() / 2);
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
    public ServerPlayerEntity player(int i) {
        if (i == 0) return leftPlayer;
        if (i == 1) return rightPlayer;

        return null;
    }

    public void player(int i, ServerPlayerEntity player) {
        if (i == 0) leftPlayer = player;
        else if (i == 1) rightPlayer = player;
    }

    public int playerIndex(ServerPlayerEntity player) {
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

    public ServerPlayerEntity opponent(int i) {
        if (i == -1) return null;

        return player(1 - i);
    }

    public boolean full() {
        return leftPlayer != null && rightPlayer != null;
    }

    public Collection<ServerPlayerEntity> players() {
        var players = new ArrayList<ServerPlayerEntity>();

        if (leftPlayer != null) players.add(leftPlayer);
        if (rightPlayer != null) players.add(rightPlayer);

        return players;
    }

    public BlockPos center() {
        return center;
    }

    public Vec3d direction() {
        return Vec3d.of(center.subtract(left));
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
