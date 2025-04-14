package work.lclpnet.lobby.game.api;

import work.lclpnet.lobby.game.api.data.GameDataPacks;
import work.lclpnet.lobby.game.api.option.GameOptionConfig;
import work.lclpnet.lobby.game.api.start.GameScope;
import work.lclpnet.lobby.game.api.start.GameStatusManager;

/// # Game
/// A game that can be selected by the game-host.
/// This class mainly describes the existence of the game, along with a [GameConfig]
/// that contains information such as identifier, title and icon.
///
/// Instances of this class are treated as singletons in the scope of the game-host.
///
/// ## GameFactory
/// A game-host will request a [GameFactory] from the game to load specific assets and then finally
/// to create an instance of the game that will then be started.
///
/// ## Data Packs
/// Optionally, a game can define bootstrap data packs that are required to play it.
/// All bootstrap data packs of all games are collected and installed before the server starts.
/// Implement [Game#getBootstrapDataPacks()] to provide data packs.
///
/// ## Game Options
/// Games can define options that are meant to be configurable, e.g. by player voting.
/// Implement [Game#configureOptions(GameOptionConfig)] to define options.
/// Options are completely handled by the game-host and are passed to the [GameFactory] on game creation.
///
/// @implNote If possible, implementations should be stateless or at least immutable.
/// This class must not be used to store any game state, use [GameInstance] instead as a new instance is
/// created every time the game is started.
public interface Game {

    GameConfig getConfig();

    boolean canBePlayed(GameScope scope);

    GameFactory createFactory();

    default GameDataPacks getBootstrapDataPacks() {
        return GameDataPacks.EMPTY;
    }

    /**
     * Configure the available options for the game.
     * Options are meant to be set / determined by the game-host and are then passed to the game instance on creation.
     * This can be used for game-map votings for example.
     * @param config The {@link GameOptionConfig} used to register options.
     */
    default void configureOptions(GameOptionConfig config) {}

    default void configureStatusManager(GameStatusManager manager) {}
}
