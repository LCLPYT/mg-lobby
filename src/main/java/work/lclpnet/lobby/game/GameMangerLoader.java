package work.lclpnet.lobby.game;

import org.jetbrains.annotations.Nullable;
import work.lclpnet.game.api.Game;

public interface GameMangerLoader {

    @Nullable
    Game getGame(String id);
}
