package work.lclpnet.lobby.game.map;

import work.lclpnet.lobby.io.copy.WorldCopier;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class DirectMapFetcher implements MapFetcher {

    private final MapLookup lookup;

    public DirectMapFetcher(MapLookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public void pull(GameMap map, Path target) throws IOException {
        URI source = lookup.getSource(map).orElseThrow();

        WorldCopier.get(source).copyTo(target);
    }
}
