package work.lclpnet.lobby.game.api;

import work.lclpnet.lobby.game.api.data.GameDataPacks;
import work.lclpnet.lobby.game.conf.GameConfig;

/**
 * A game that can be selected by the game runtime.
 * This class mainly describes the existence of the game, along with a {@link GameConfig},
 * that contains information such as identifier, title and icon.
 * A game runtime can also request a {@link GameFactory} that is used to create an actual instance of the game that can be started.
 * Optionally, a game can define bootstrap data packs that are required to play it.
 * All bootstrap data packs of all games are collected and installed before the server starts.
 * <br>
 * Instances of this class are treated as singletons.
 * @implNote If possible, implementations should be stateless or at least immutable.
 * This class must not be used to store any game state, use {@link GameInstance} instead as a new instance is created every time.
 */
public interface Game {

    GameConfig getConfig();

    GameFactory createFactory();

    default GameDataPacks getBootstrapDataPacks() {
        return GameDataPacks.EMPTY;
    }
}
