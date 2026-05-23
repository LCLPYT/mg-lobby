package work.lclpnet.game.api;

/**
 * Represents an instance of a game.
 * A game instance is created when the game-host starts the game, i.e. when the lobby countdown is over.
 * If the game-host is a lobby, the lobby usually has a duration to wait for players to join.
 * In that phase, the {@link GameFactory} of the game is responsible for handling any pre-game configuration, e.g. map-voting, team selection and similar.
 * Those configuration options should then be passes to the {@link GameInstance} implementation by the {@link GameFactory}.
 */
public interface GameInstance {

    void start();
}
