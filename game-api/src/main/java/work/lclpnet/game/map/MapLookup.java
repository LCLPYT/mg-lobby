package work.lclpnet.game.map;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

public interface MapLookup {

    Collection<GameMap> getMaps(MapDescriptor descriptor) throws IOException;

    Iterable<URI> getSource(GameMap map) throws IOException;
}
