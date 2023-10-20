package work.lclpnet.lobby.game;

/**
 * An SPI for the {@link Game} interface.
 * Used by the game runtime to locate and instantiate games.
 */
public interface GameProvider {

    Game provideGame();
}
