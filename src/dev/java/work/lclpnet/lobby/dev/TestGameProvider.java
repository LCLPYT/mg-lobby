package work.lclpnet.lobby.dev;

import work.lclpnet.lobby.game.api.Game;
import work.lclpnet.lobby.game.api.GameProvider;

public class TestGameProvider implements GameProvider {

    @Override
    public Game provideGame() {
        return new TestGame();
    }
}
