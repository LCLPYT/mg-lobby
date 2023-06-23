package work.lclpnet.lobby.decor.ttt;

public class TicTacToeInstance {

    private final TicTacToe game;
    private int nextTurn;

    public TicTacToeInstance(TicTacToe game, int beginner) {
        if (beginner == -1) throw new IllegalArgumentException("Invalid beginner");

        this.game = game;
        this.nextTurn = beginner;
    }

    public boolean isPlayersTurn(int player) {
        return nextTurn == player;
    }

    public boolean play(int player, int x, int y) {
        boolean valid = game.doMove(player + 1, x, y);
        if (!valid) return false;

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
}
