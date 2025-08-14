package work.lclpnet.lobby.game.map;

import com.google.common.collect.Iterables;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import work.lclpnet.kibu.hook.Hook;
import work.lclpnet.kibu.hook.HookFactory;
import work.lclpnet.lobby.game.asset.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AssetMapRepository implements MapRepository {

    public static final String CACHED_PROPERTY = "__cached";

    private final AssetRepository assetRepository;
    private final Logger logger;
    private volatile Hook<MapRedirectAction> redirectActionHook = null;

    public AssetMapRepository(AssetRepository assetRepository, Logger logger) {
        this.assetRepository = assetRepository;
        this.logger = logger;
    }

    @Override
    public Collection<MapRef> getMapList(String path) throws IOException {
        var index = fetchJsonObject(AssetPath.of(path, "index.json"));
        JSONArray mapsArray = index.value().getJSONArray("maps");

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

    private Result<JSONObject> fetchJsonObject(AssetPath assetPath) throws IOException {
        AssetStreamResource res = assetRepository.getStream(assetPath);

        String content;

        try (res) {
            content = new String(res.resource().readAllBytes(), StandardCharsets.UTF_8);
        }

        var json = new JSONObject(content);

        return new Result<>(json, res.cached());
    }

    @Override
    public MapInfo getMapInfo(String path) throws IOException {
        var res = getMapInfo(AssetPath.of(), path, 5);

        res.value().properties().put(CACHED_PROPERTY, res.cached);

        return res.value();
    }

    private Result<MapInfo> getMapInfo(AssetPath root, String path, final int maxLinkDepth) throws IOException {
        AssetPath mapPath = root.resolve(path);
        AssetPath assetPath = mapPath.resolve("map.json");

        var res = fetchJsonObject(assetPath);
        JSONObject props = res.value();

        MapInfo currentInfo = new MapInfo(mapPath.toString(), props, this);
        String target = props.optString("target", null);

        if (target == null) {
            return new Result<>(currentInfo, res.cached());
        }

        if (maxLinkDepth <= 0) {
            throw new IOException("Too many links");
        }

        AssetPath base;

        if (target.startsWith("/")) {
            base = AssetPath.of();
            target = target.substring(1);
        } else {
            base = mapPath;
        }

        if (redirectActionHook != null) {
            redirectActionHook.invoker().visit(path, currentInfo);
        }

        var info = getMapInfo(base, target, maxLinkDepth - 1);
        info.value().merge(props);

        return new Result<>(info.value(), res.cached() && info.cached());
    }

    @Override
    public InputStream open(String path, AssetRequestOptions options) throws IOException {
        AssetPath assetPath = AssetPath.of(path);

        return assetRepository.getStream(assetPath, options).resource();
    }

    @Override
    public Iterable<URI> getUris(String path, AssetRequestOptions options) {
        AssetPath assetPath = AssetPath.of(path);

        return Iterables.transform(
                assetRepository.getUris(assetPath, options),
                res -> res != null ? res.resource() : null
        );
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

    private record Result<T>(T value, boolean cached) {}
}
