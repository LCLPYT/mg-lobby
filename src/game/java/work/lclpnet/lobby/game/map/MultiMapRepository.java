package work.lclpnet.lobby.game.map;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MultiMapRepository implements MapRepository {

    private final MapRepository[] children;

    public MultiMapRepository(MapRepository[] children) {
        this.children = children;
    }

    @Override
    public Collection<MapRef> getMapList(String path) {
        Set<MapRef> refs = new HashSet<>();

        for (MapRepository repo : children) {
            try {
                refs.addAll(repo.getMapList(path));
            } catch (IOException ignored) {}
        }

        return refs;
    }

    @Override
    public MapInfo getMapInfo(String path) throws IOException {
        for (MapRepository repo : children) {
            try {
                return repo.getMapInfo(path);
            } catch (IOException ignored) {}
        }

        throw new IOException("Map information wasn't found");
    }

    @Override
    public Optional<URI> getResource(String path, String resource) {
        for (MapRepository child : children) {
            var uri = child.getResource(path, resource);

            if (uri.isPresent()) {
                return uri;
            }
        }

        return Optional.empty();
    }
}
