package work.lclpnet.lobby.decor.ttt;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class TicTacToeInstance {

    private final TicTacToe game;
    private final int initiator;
    private int nextTurn;
    private BlockState[] colors;
    private boolean begun = false;

    public TicTacToeInstance(TicTacToe game, int beginner, BlockState[] colors) {
        if (beginner == -1) throw new IllegalArgumentException("Invalid beginner");

        this.game = game;
        this.nextTurn = beginner;

        this.initiator = beginner;

        assignUniqueColors(colors, beginner);
    }

    private void assignUniqueColors(BlockState[] colors, int beginner) {
        // make sure every player has a unique color; players with lower index have a higher priority
        this.colors = new BlockState[colors.length];

        this.colors[beginner] = colors[beginner];

        final int opponent = 1 - beginner;

        if (colors[opponent] != colors[beginner]) {
            this.colors[opponent] = colors[opponent];
            return;
        }

        BlockState fallback = Blocks.RED_GLAZED_TERRACOTTA.getDefaultState();

        if (colors[beginner] != fallback) {
            this.colors[opponent] = fallback;
            return;
        }

        // beginner chose the fallback, assign a second fallback
        this.colors[opponent] = Blocks.BLUE_GLAZED_TERRACOTTA.getDefaultState();
    }

    public boolean isPlayersTurn(int player) {
        if (isGameOver()) return false;

        return nextTurn == player;
    }

    public boolean play(int player, int x, int y) {
        boolean valid = game.doMove(player + 1, x, y);
        if (!valid) return false;

        begun = true;

        nextTurn = 1 - player;

        return true;
    }

    public boolean isGameOver() {
        return game.isGameOver();
    }

    public int getWinner() {
        int winner = game.getWinner();
        if (winner == -1) return -1;

        return winner - 1;
    }

    public BlockState getDisplayBlock(int player) {
        return colors[player];
    }

    public int getInitiator() {
        return initiator;
    }

    public boolean hasBegun() {
        return begun;
    }
}
