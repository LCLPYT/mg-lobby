package work.lclpnet.lobby.game.map;

import java.io.IOException;
import java.util.Set;

public interface MapRepository {

    Set<GameMap> getMaps(String namespace) throws IOException;
}
