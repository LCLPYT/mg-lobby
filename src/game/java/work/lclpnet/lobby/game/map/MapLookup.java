package work.lclpnet.lobby.game.map;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;

public interface MapLookup {

    Collection<GameMap> getMaps(MapDescriptor descriptor) throws IOException;

    Optional<URI> getSource(GameMap map) throws IOException;
}
