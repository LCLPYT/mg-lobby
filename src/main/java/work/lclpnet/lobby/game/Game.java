package work.lclpnet.lobby.game;

import work.lclpnet.lobby.game.conf.GameConfig;

public interface Game {

    GameConfig getConfig();

    boolean canStart();

    void start();
}
