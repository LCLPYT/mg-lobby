package work.lclpnet.lobby.game.api;

/**
 * An interface that can be used to end a game.
 * @implNote All related instance data should be closed when the game is finished.
 */
public interface GameFinisher {

    void finishGame(Reason reason);

    default void finishGame() {
        finishGame(Reason.REGULAR);
    }

    enum Reason {
        REGULAR,
        COMMAND,
        UNLOADED
    }
}
