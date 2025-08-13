package work.lclpnet.lobby.game.map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import work.lclpnet.kibu.hook.Hook;
import work.lclpnet.kibu.hook.HookFactory;
import work.lclpnet.lobby.game.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UriMapRepository implements MapRepository {

    private final URI root;
    private final Logger logger;
    private volatile Hook<MapRedirectAction> redirectActionHook = null;

    public UriMapRepository(URI root, Logger logger) {
        this.root = root;
        this.logger = logger;
    }

    @Deprecated
    @Override
    public Collection<MapRef> getMapList(String path) throws IOException {
        URI indexUri = root.resolve(path + "/index.json");

        if (!indexUri.getPath().startsWith(root.getPath())) {
            throw new IOException("Path outside of repository");
        }

        JSONObject index = fetchJsonObject(indexUri);
        JSONArray mapsArray = index.getJSONArray("maps");

        Set<MapRef> maps = new HashSet<>();

        for (Object obj : mapsArray) {
            if (!(obj instanceof JSONObject json)) {
                logger.warn("Invalid json map array entry");
                continue;
            }

            maps.add(new MapRef(json));
        }

        return maps;
    }

    @Override
    public MapInfo getMapInfo(String path) throws IOException {
        return getMapInfo(root, path, 5);
    }

    @Override
    public InputStream open(String path) throws IOException {
        throw new IOException("unsupported");
    }

    private Optional<URI> getResourceUnchecked(String path, String resource) {
        if (!resource.isEmpty() && resource.charAt(0) == '/') {
            String ref = resource.substring(1);
            return Optional.of(root.resolve(ref));
        }

        URI base = root.resolve(path + "/");

        return FileUtil.getUri(base, resource);
    }

    @Override
    public void addRedirectAction(MapRedirectAction action) {
        hook().register(action);
    }

    private Hook<MapRedirectAction> hook() {
        if (redirectActionHook != null) {
            return redirectActionHook;
        }

        synchronized (this) {
            if (redirectActionHook == null) {
                redirectActionHook = HookFactory.createArrayBacked(MapRedirectAction.class, actions -> (path, info) -> {
                    for (MapRedirectAction action : actions) {
                        action.visit(path, info);
                    }
                });
            }
        }

        return redirectActionHook;
    }

    private MapInfo getMapInfo(URI root, String path, final int maxLinkDepth) throws IOException {
        URI mapUri = root.resolve(path + "/map.json");

        if (!insideRepository(mapUri)) {
            throw new IOException("Path outside of repository");
        }

        JSONObject props = fetchJsonObject(mapUri);

        URI rootRelative = this.root.relativize(root.resolve(path));
        String rootPath = Objects.requireNonNull(rootRelative.getPath());

        MapInfo currentInfo = new MapInfo(rootPath, props, this);
        String target = props.optString("target", null);

        if (target == null) {
            return currentInfo;
        }

        if (maxLinkDepth <= 0) {
            throw new IOException("Too many links");
        }

        URI base;

        if (target.startsWith("/")) {
            base = this.root;
            target = target.substring(1);
        } else {
            base = mapUri;
        }

        if (redirectActionHook != null) {
            redirectActionHook.invoker().visit(path, currentInfo);
        }

        MapInfo info = getMapInfo(base, target, maxLinkDepth - 1);
        info.merge(props);

        return info;
    }

    private boolean insideRepository(URI uri) {
        return uri.getPath().startsWith(this.root.getPath());
    }

    public URI getRoot() {
        return root;
    }

    private JSONObject fetchJsonObject(URI uri) throws IOException {
        String content;

        try (InputStream in = uri.toURL().openStream()) {
            content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        return new JSONObject(content);
    }
}
