package work.lclpnet.lobby.game;

/**
 * Represents an instance of a game.
 * An instance is created when the game runtime creates a new instance of a game type.
 * This happens right in the lobby, even before the countdown starts.
 * The first lifecycle phase is the starting phase, in which a {@link GameStarter} manages the pre-game stuff,
 * such as map voting or team choosing.
 * When the starter decides the game is ready, it signals the game runtime.
 * The game runtime then starts the actual game instance by invoking {@link GameInstance#start()}.
 */
public interface GameInstance {

    GameStarter createStarter(GameStarter.Args args, GameStarter.Callback onStart);

    void start();
}
