package work.lclpnet.lobby.game.map;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;

public interface MapRepository {

    Collection<MapRef> getMapList(String path) throws IOException;

    MapInfo getMapInfo(String path) throws IOException;

    /**
     * Gets a resource relative to path.
     * @param path The path that is used to resolve the resource.
     * @param resource The resource to get.
     * @return An optional URI to the resource.
     * The resource might still not exist, as repositories are not required to check whether the resource is available.
     */
    Optional<URI> getResource(String path, String resource);

    /**
     * Gets a resource relative to a map described by a given {@link MapInfo}.
     * Usually the same as {@link #getResource(String, String)}.
     * @param info The map info descriptor.
     * @param resource The resource to get.
     * @return An optional URI to the resource.
     * The resource might still not exist, as repositories are not required to check whether the resource is available.
     */
    default Optional<URI> getResource(MapInfo info, String resource) {
        return getResource(info.target() + "/", resource);
    }

    default void addRedirectAction(MapRedirectAction action) {
        throw new UnsupportedOperationException("MapRepository does not support redirect actions");
    }
}
