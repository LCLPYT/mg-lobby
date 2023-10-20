package work.lclpnet.lobby.dev;

import work.lclpnet.lobby.game.Game;
import work.lclpnet.lobby.game.GameProvider;

public class TestGameProvider implements GameProvider {

    @Override
    public Game provideGame() {
        return new TestGame();
    }
}
