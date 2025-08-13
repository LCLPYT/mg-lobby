package work.lclpnet.lobby.game.map;

import work.lclpnet.lobby.io.copy.WorldCopier;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectMapFetcher implements MapFetcher {

    private final MapLookup lookup;

    public DirectMapFetcher(MapLookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public void pull(GameMap map, Path target) throws IOException {
        Path tmp;

        try (InputStream in = lookup.openSource(map).orElseThrow()) {
            tmp = Files.createTempFile("game-commons-maps-dl", ".tmp");

            try (var out = Files.newOutputStream(tmp)) {
                in.transferTo(out);
            }
        }

        URI uri = tmp.toUri();

        WorldCopier.get(uri).copyTo(target);
    }
}
