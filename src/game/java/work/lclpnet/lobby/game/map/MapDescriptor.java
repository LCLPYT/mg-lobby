package work.lclpnet.lobby.game.map;

import net.minecraft.util.Identifier;

import java.util.Objects;

public class MapDescriptor {

    public static final String NAMESPACE_REGEX = "^[a-z0-9_.-]+$";
    public static final String PATH_REGEX = "^[a-z0-9/._-]*$";
    public static final String VERSION_REGEX = "^[a-z0-9_.-]*$";
    private final String namespace;
    private final String path;
    private final String version;

    public MapDescriptor(Identifier identifier, String version) {
        this(identifier.getNamespace(), identifier.getPath(), version);
    }

    public MapDescriptor(String namespace, String path, String version) {
        this.namespace = namespace;
        this.path = path;
        this.version = version;

        if (!namespace.matches(NAMESPACE_REGEX)) {
            throw new IllegalArgumentException("Namespace does not match " + NAMESPACE_REGEX);
        }

        if (!path.matches(PATH_REGEX)) {
            throw new IllegalArgumentException("Path does not match " + PATH_REGEX);
        }

        if (!version.matches(VERSION_REGEX)) {
            throw new IllegalArgumentException("Version does not match " + VERSION_REGEX);
        }
    }

    public Identifier getIdentifier() {
        return new Identifier(namespace, path);
    }

    public String getMapPath() {
        String mapPath = namespace;

        if (!path.isEmpty()) {
            mapPath += '/' + path;
        }

        if (!version.isEmpty()) {
            mapPath += '/' + version;
        }

        return mapPath;
    }

    public String getVersion() {
        return version;
    }

    public MapDescriptor resolve(String suffix) {
        if (suffix.startsWith("/")) {
            String abs = suffix.substring(1);

            int nextSlash = abs.indexOf('/');

            if (nextSlash == -1) {
                return new MapDescriptor(abs, "", version);
            }

            String absNamespace = abs.substring(0, nextSlash);
            String absPath = abs.substring(nextSlash + 1);

            return new MapDescriptor(absNamespace, absPath, version);
        }

        String appended = path.isEmpty() ? suffix : path + '/' + suffix;

        return new MapDescriptor(namespace, appended, version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapDescriptor that = (MapDescriptor) o;
        return Objects.equals(namespace, that.namespace) && Objects.equals(path, that.path) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, path, version);
    }
}
