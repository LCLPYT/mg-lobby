package work.lclpnet.lobby.game.api;

/**
 * An SPI for the {@link Game} interface.
 * Used by the game runtime to locate and instantiate games.
 */
public interface GameProvider {

    Game provideGame();
}
