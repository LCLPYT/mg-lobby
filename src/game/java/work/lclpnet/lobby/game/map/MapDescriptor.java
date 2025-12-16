package work.lclpnet.lobby.game.map;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class MapDescriptor {

    public static final String NAMESPACE_REGEX = "^[a-z0-9_.-]+$";
    public static final String PATH_REGEX = "^[a-z0-9/._-]*$";
    private final String namespace;
    private final String path;

    public MapDescriptor(ResourceLocation identifier) {
        this(identifier.getNamespace(), identifier.getPath());
    }

    public MapDescriptor(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;

        if (!namespace.matches(NAMESPACE_REGEX)) {
            throw new IllegalArgumentException("Namespace does not match " + NAMESPACE_REGEX);
        }

        if (!path.matches(PATH_REGEX)) {
            throw new IllegalArgumentException("Path does not match " + PATH_REGEX);
        }
    }

    public ResourceLocation getIdentifier() {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public String getMapPath() {
        String mapPath = namespace;

        if (!path.isEmpty()) {
            mapPath += '/' + path;
        }

        return mapPath;
    }

    public MapDescriptor resolve(String suffix) {
        if (suffix.startsWith("/")) {
            String abs = suffix.substring(1);

            int nextSlash = abs.indexOf('/');

            if (nextSlash == -1) {
                return new MapDescriptor(abs, "");
            }

            String absNamespace = abs.substring(0, nextSlash);
            String absPath = abs.substring(nextSlash + 1);

            return new MapDescriptor(absNamespace, absPath);
        }

        String appended = path.isEmpty() ? suffix : path + '/' + suffix;

        return new MapDescriptor(namespace, appended);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapDescriptor that = (MapDescriptor) o;
        return Objects.equals(namespace, that.namespace) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
