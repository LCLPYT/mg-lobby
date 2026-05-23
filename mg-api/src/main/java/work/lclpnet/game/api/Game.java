package work.lclpnet.game.api;

import org.jetbrains.annotations.NotNull;
import work.lclpnet.game.api.data.GameDataPacks;
import work.lclpnet.game.api.start.GameStartScope;
import work.lclpnet.game.api.start.GameStatusManager;

/// # Game
/// A game that can be selected by the game-host.
/// This class mainly describes the existence of the game, along with a [GameConfig]
/// that contains information such as identifier, title and icon.
///
/// Instances of this class are treated as singletons in the scope of the game-host.
///
/// ## GameFactory
/// A game-host will request a [GameFactory] from the game once it is selected.
/// It manages things like game-specific translations, game starting activities while still in lobby (map voting, team selection etc.).
/// Finally, it is also responsible for creating the game instance that will then be started.
///
/// ## Data Packs
/// Optionally, a game can define bootstrap data packs that are required to play it.
/// All bootstrap data packs of all games are collected and installed before the server starts.
/// Implement [Game#getBootstrapDataPacks()] to provide data packs.
///
/// @implNote If possible, implementations should be stateless or at least immutable.
/// This class must not be used to store any game state, use [GameFactory] / [GameInstance] instead as a new instance is
/// created every time the game is selected / started.
public interface Game {

    /**
     * Create the game config for this game.
     * It specifies mostly the game id, title translation key and icon.
     * @return The game config.
     */
    @NotNull GameConfig getConfig();

    /// Evaluates whether the came can be played / started right now.
    /// This may be used to impose a minimum player limit, for example.
    /// @param scope The scope of the game start. Containing information like player count etc.
    boolean canBePlayed(@NotNull GameStartScope scope);

    /**
     * Creates the game factory.
     * Called when the game is selected by the game host and once
     * @return The game factory for this game.
     */
    @NotNull GameFactory createFactory();

    /**
     * Provides minigames the possibility to register required data packs.
     * Those datapacks will be gathered and installed before the server starts, so that all datapack features can be used.
     * Otherwise, non-reloadable registries would not be able to be adjusted by datapacks, as those are constructed once at server load.
     * @return An implementation of the {@link GameDataPacks} interface.
     */
    default @NotNull GameDataPacks getBootstrapDataPacks() {
        return GameDataPacks.EMPTY;
    }

    /**
     * Provides an API for configuring status messages.
     * Periodic error messages for when the game cannot be started yet and the bossbar title can be configured.
     * {@link work.lclpnet.game.util.GameStartUtil} is provided as an easy way to configure commonly used messages.
     * @param manager The {@link GameStatusManager} that is used to configure status messages.
     */
    default void configureStatusManager(@NotNull GameStatusManager manager) {}
}
