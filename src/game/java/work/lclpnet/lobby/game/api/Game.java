package work.lclpnet.lobby.game.api;

import work.lclpnet.lobby.game.conf.GameConfig;
import work.lclpnet.mplugins.ext.PluginUnloader;

/**
 * A game type that can be chosen by the game runtime.
 * There is only one persistent instance of every {@link Game} per game runtime.
 * @implNote Implementations should be pretty minimal in a sense that they should not have any side effects or state.
 * As an instance of every implementation is kept at all times.
 * Put instance related data into the actual {@link GameInstance}, which is only created when the game is actually
 * requested by the runtime.
 */
public interface Game {

    GameConfig getConfig();

    PluginUnloader getOwner();

    GameInstance createInstance(GameEnvironment environment);
}
