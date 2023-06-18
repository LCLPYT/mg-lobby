package work.lclpnet.lobby.game.start;

public interface GameStarter {

    void init();

    void start();

    boolean isStarted();

    void destroy();
}
