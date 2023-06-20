package work.lclpnet.lobby.decor.ttt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicTacToeTest {

    private static final int PLAYER_A = 1, PLAYER_B = 2;

    private TicTacToe createGame() {
        return null;
    }

    @Test
    void initialGame() {
        var game = createGame();
        assertFalse(game.isGameOver());
        assertEquals(-1, game.getWinner());  // not important while game is not over, but check anyway
    }

    @Test
    void invalidOutOfBounds() {
        var game = createGame();

        assertFalse(game.doMove(PLAYER_A, -1, 0));
        assertFalse(game.doMove(PLAYER_B, 1, 3));
        assertFalse(game.doMove(PLAYER_B, -2, -1));
        assertFalse(game.doMove(PLAYER_B, 4, 3));
    }

    @Test
    void basicGame() {
        var game = createGame();

        assertTrue(game.doMove(PLAYER_A, 0, 0));
        assertTrue(game.doMove(PLAYER_A, 1, 0));
        assertTrue(game.doMove(PLAYER_A, 2, 0));

        assertTrue(game.isGameOver());
        assertEquals(PLAYER_A, game.getWinner());
    }

    @Test
    void advancedGame() {
        var game = createGame();

        assertTrue(game.doMove(PLAYER_A, 0, 0));

        assertFalse(game.doMove(PLAYER_B, 0, 0));
        assertTrue(game.doMove(PLAYER_B, 2, 2));

        assertTrue(game.doMove(PLAYER_A, 0, 1));

        assertTrue(game.doMove(PLAYER_B, 0, 2));

        assertFalse(game.isGameOver());

        assertFalse(game.doMove(PLAYER_A, 0, 2));
        assertTrue(game.doMove(PLAYER_A, 2, 0));

        assertTrue(game.doMove(PLAYER_B, 1, 2));

        assertTrue(game.isGameOver());
        assertEquals(PLAYER_B, game.getWinner());
    }

    @Test
    void draw() {
        var game = createGame();

        assertTrue(game.doMove(PLAYER_A, 0, 1));
        assertTrue(game.doMove(PLAYER_B, 2, 0));
        assertTrue(game.doMove(PLAYER_A, 1, 0));
        assertTrue(game.doMove(PLAYER_B, 0, 0));
        assertTrue(game.doMove(PLAYER_A, 1, 1));
        assertTrue(game.doMove(PLAYER_B, 2, 1));
        assertTrue(game.doMove(PLAYER_A, 2, 2));
        assertTrue(game.doMove(PLAYER_B, 1, 2));
        assertTrue(game.doMove(PLAYER_A, 0, 2));

        assertTrue(game.isGameOver());
        assertEquals(0, game.getWinner());
    }

    @Test
    void winFullBoard() {
        var game = createGame();

        assertTrue(game.doMove(PLAYER_A, 0, 0));
        assertTrue(game.doMove(PLAYER_B, 1, 2));
        assertTrue(game.doMove(PLAYER_A, 0, 1));
        assertTrue(game.doMove(PLAYER_B, 1, 1));
        assertTrue(game.doMove(PLAYER_A, 1, 0));
        assertTrue(game.doMove(PLAYER_B, 2, 0));
        assertTrue(game.doMove(PLAYER_A, 2, 2));
        assertTrue(game.doMove(PLAYER_B, 2, 1));
        assertTrue(game.doMove(PLAYER_A, 0, 2));

        assertTrue(game.isGameOver());
        assertEquals(PLAYER_A, game.getWinner());
    }

    @Test
    void winColumn() {
        var game = createGame();

        assertTrue(game.doMove(PLAYER_A, 0, 0));
        assertTrue(game.doMove(PLAYER_A, 0, 1));
        assertTrue(game.doMove(PLAYER_A, 0, 2));

        assertTrue(game.isGameOver());
        assertEquals(PLAYER_A, game.getWinner());
    }

    @Test
    void winRow() {
        var game = createGame();

        assertTrue(game.doMove(PLAYER_B, 0, 0));
        assertTrue(game.doMove(PLAYER_B, 1, 0));
        assertTrue(game.doMove(PLAYER_B, 2, 0));

        assertTrue(game.isGameOver());
        assertEquals(PLAYER_B, game.getWinner());
    }

    @Test
    void winFallingDiagonal() {
        var game = createGame();

        assertTrue(game.doMove(PLAYER_A, 0, 0));
        assertTrue(game.doMove(PLAYER_A, 1, 1));
        assertTrue(game.doMove(PLAYER_A, 2, 2));

        assertTrue(game.isGameOver());
        assertEquals(PLAYER_A, game.getWinner());
    }

    @Test
    void winRisingDiagonal() {
        var game = createGame();

        assertTrue(game.doMove(PLAYER_A, 0, 2));
        assertTrue(game.doMove(PLAYER_A, 1, 1));
        assertTrue(game.doMove(PLAYER_A, 2, 0));

        assertTrue(game.isGameOver());
        assertEquals(PLAYER_A, game.getWinner());
    }
}