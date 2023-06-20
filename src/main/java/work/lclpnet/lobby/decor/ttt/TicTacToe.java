package work.lclpnet.lobby.decor.ttt;

public interface TicTacToe {

    /**
     * Performs a move.
     * @param player The player that performs the move.
     * @param x x-coordinate
     * @param y y-coordinate
     * @return True, if the move was valid, false otherwise.
     */
    boolean doMove(int player, int x, int y);

    /**
     * Checks if the game is over.
     * @return True, if there is a winner, or if the match is a draw.
     */
    boolean isGameOver();

    /**
     * This method returns the player who won, or -1 if the game is a draw.
     * @return The winner, or -1 if there is none.
     */
    int getWinner();
}
