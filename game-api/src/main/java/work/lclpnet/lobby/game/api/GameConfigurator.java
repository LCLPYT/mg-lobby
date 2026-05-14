package work.lclpnet.lobby.game.api;

import work.lclpnet.lobby.game.api.option.GameOptionConfig;

/**
 * An interface for configuring the upcoming instance of a {@link work.lclpnet.lobby.game.api.Game}.
 * It is optional for games to implement this interface.
 */
public interface GameConfigurator {

    /**
     * Configure the available options of the game.
     * Options are meant to be set / determined by the game-host and are then passed to the game instance on creation.
     * This can be used for game-map votings for example.
     * @param config The {@link GameOptionConfig} used to register options.
     */
    void configureOptions(GameOptionConfig config);
}
