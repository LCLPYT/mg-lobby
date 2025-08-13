package work.lclpnet.lobby.game.map;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import work.lclpnet.kibu.hook.Hook;
import work.lclpnet.kibu.hook.HookFactory;
import work.lclpnet.lobby.game.asset.AssetPath;
import work.lclpnet.lobby.game.asset.AssetRepository;
import work.lclpnet.lobby.game.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AssetMapRepository implements MapRepository {

    private final AssetRepository assetRepository;
    private final Logger logger;
    private volatile Hook<MapRedirectAction> redirectActionHook = null;

    public AssetMapRepository(AssetRepository assetRepository, Logger logger) {
        this.assetRepository = assetRepository;
        this.logger = logger;
    }

    @Override
    public Collection<MapRef> getMapList(String path) throws IOException {
        JSONObject index = fetchJsonObject(AssetPath.of(path, "index.json"));
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

    private @NotNull JSONObject fetchJsonObject(AssetPath assetPath) throws IOException {
        String content;

        try (InputStream in = assetRepository.open(assetPath)) {
            content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        return new JSONObject(content);
    }

    @Override
    public MapInfo getMapInfo(String path) throws IOException {
        return getMapInfo(AssetPath.of(), path, 5);
    }

    private MapInfo getMapInfo(AssetPath root, String path, final int maxLinkDepth) throws IOException {
        AssetPath mapPath = root.resolve(path);
        AssetPath assetPath = mapPath.resolve("map.json");
        JSONObject props = fetchJsonObject(assetPath);

        MapInfo currentInfo = new MapInfo(mapPath.toString(), props, this);
        String target = props.optString("target", null);

        if (target == null) {
            return currentInfo;
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

        MapInfo info = getMapInfo(base, target, maxLinkDepth - 1);
        info.merge(props);

        return info;
    }

    @Override
    public InputStream open(String path) throws IOException {
        AssetPath assetPath = AssetPath.of(path);

        return assetRepository.open(assetPath);
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
}
