package work.lclpnet.lobby.game;

import work.lclpnet.lobby.game.api.Game;

import javax.annotation.Nullable;

public interface GameMangerLoader {

    @Nullable
    Game getGame(String id);
}
