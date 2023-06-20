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
     * @return True, if there is a winner, or if the match is a draw. False if the game is still being played.
     */
    boolean isGameOver();

    /**
     * This method returns the player who won.
     * @return The winner; 0 when the game is a draw. -1 if the game is not set yet.
     */
    int getWinner();
}
