package work.lclpnet.lobby.game.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

public interface MapLookup {

    Collection<GameMap> getMaps(MapDescriptor descriptor) throws IOException;

    Optional<InputStream> openSource(GameMap map) throws IOException;
}
