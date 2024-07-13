package work.lclpnet.lobby.game.map;

import org.jetbrains.annotations.Nullable;

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
            MapInfo info;

            try {
                info = repo.getMapInfo(path);
            } catch (IOException ignored) {
                continue;
            }

            if (info.origin() == repo) {
                return info;
            }

            return info.withOrigin(repo);
        }

        throw new IOException("Map information wasn't found");
    }

    @Override
    public Optional<URI> getResource(String path, String resource) {
        return getResourceFromChildren(path, resource, null);
    }

    @Override
    public Optional<URI> getResource(MapInfo info, String resource) {
        String path = info.target() + "/";

        // if we know the map info origin, query that repository first
        MapRepository origin = info.origin();

        if (origin != null) {
            var originResource = origin.getResource(path, resource);

            if (originResource.isPresent()) {
                return originResource;
            }
        }

        // query the other children
        return getResourceFromChildren(path, resource, origin);
    }

    private Optional<URI> getResourceFromChildren(String path, String resource, @Nullable MapRepository exclude) {
        for (MapRepository child : children) {
            if (child == exclude) continue;

            var uri = child.getResource(path, resource);

            if (uri.isPresent()) {
                return uri;
            }
        }

        return Optional.empty();
    }
}
