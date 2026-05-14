package work.lclpnet.lobby.game.api;

import work.lclpnet.lobby.game.api.option.GameOptions;

/**
 * Represents an instance of a game.
 * A game instance is created when the game-host starts the game, i.e. when the lobby countdown is over.
 * If the game-host is a lobby, the lobby usually has a duration to wait for players to join.
 * In that phase, the game-host is responsible for handling any game configuration, e.g. for map-votings and similar.
 * Those configuration options are then passed to the instance via the GameFactory.
 */
public interface GameInstance {

    void start(GameOptions options);
}
