package work.lclpnet.lobby.game.map;

import net.minecraft.util.Identifier;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface MapRepository {

    Set<GameMap> getMaps(String namespace) throws IOException;

    Map<String, Object> getData(Identifier identifier) throws IOException;

    Optional<URI> getMapSource(Map<String, Object> data, Identifier identifier) throws IOException;
}
