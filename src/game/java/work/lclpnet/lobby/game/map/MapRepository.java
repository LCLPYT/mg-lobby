package work.lclpnet.lobby.game.map;

import net.minecraft.util.Identifier;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

public interface MapRepository {

    Set<GameMap> getMaps(String namespace) throws IOException;

    URI getMapSource(Identifier identifier) throws IOException;
}
