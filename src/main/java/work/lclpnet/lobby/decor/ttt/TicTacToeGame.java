package work.lclpnet.lobby.decor.ttt;

public class TicTacToeGame implements TicTacToe {

    private final int[][] gameArr = new int[3][3];
    private int winner = -1;

    @Override
    public boolean doMove(int player, int x, int y) {
        if (x < 0 || x >= gameArr.length || y < 0 || y >= gameArr.length || gameArr[x][y] != 0) return false;

        gameArr[x][y] = player;

        return true;
    }

    @Override
    public boolean isGameOver() {
        for (int i = 0; i < 3; i++) {
            // check rows for streak
            boolean streak = true;

            for (int j = 0; j < 3; j++) {
                if (gameArr[i][j] != gameArr[i][0] || gameArr[i][0] == 0) {
                    streak = false;
                    break;
                }
            }

            if (streak) {
                winner = gameArr[i][0];
                return true;
            }

            // check columns for streak
            streak = true;

            for (int k = 0; k < 3; k++) {
                if (gameArr[k][i] != gameArr[0][i] || gameArr[0][i] == 0) {
                    streak = false;
                    break;
                }
            }

            if (streak) {
                winner = gameArr[0][i];
                return true;
            }

            // check left to right diagonal for streak
            streak = true;

            for (int j = 0; j < 3; j++) {
                if (gameArr[j][j] != gameArr[0][0] || gameArr[j][j] == 0) {
                    streak = false;
                    break;
                }
            }

            if (streak) {
                winner = gameArr[0][0];
                return true;
            }

            // check right to left diagonal for streak
            streak = true;

            for (int k = 0; k < 3; k++) {
                if (gameArr[k][2 - k] != gameArr[0][2] || gameArr[k][2 - k] == 0) {
                    streak = false;
                    break;
                }
            }

            if (streak) {
                winner = gameArr[0][2];
                return true;
            }
        }

        // check draw
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (gameArr[i][j] == 0) {
                    return false;
                }
            }
        }

        winner = 0;

        return true;
    }

    @Override
    public int getWinner() {
        return winner;
    }
}


