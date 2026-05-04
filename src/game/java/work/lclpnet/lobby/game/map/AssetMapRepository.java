package work.lclpnet.lobby.game.map;

import com.google.common.collect.Iterables;
import net.fabricmc.loader.api.Version;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import work.lclpnet.gaco.asset.AssetPath;
import work.lclpnet.gaco.asset.AssetRepository;
import work.lclpnet.gaco.asset.AssetRequestOptions;
import work.lclpnet.gaco.asset.AssetStreamResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class AssetMapRepository implements MapRepository {

    public static final String CACHED_PROPERTY = "__cached";

    private final AssetRepository assetRepository;
    private final Map<String, Version> moduleVersions;
    private final Logger logger;

    public AssetMapRepository(AssetRepository assetRepository, Map<String, Version> moduleVersions, Logger logger) {
        this.assetRepository = assetRepository;
        this.moduleVersions = moduleVersions;
        this.logger = logger;
    }

    @Override
    public Collection<MapRef> getMapList(AssetPath path) throws IOException {
        var index = fetchJsonObject(path.resolve("index.json"));
        JSONArray mapsArray = index.value().getJSONArray("maps");

        Set<MapRef> maps = new HashSet<>();

        for (Object obj : mapsArray) {
            if (!(obj instanceof JSONObject json)) {
                logger.warn("Invalid json map array entry");
                continue;
            }

            MapRef mapRef = MapRef.create(json, logger).orElse(null);

            if (mapRef == null) continue;

            Consumer<String> reporter = error ->
                    logger.debug("Ignoring map {}: Version constraint mismatch: {}", mapRef.path(), error);

            if (mapRef.dependencies().matches(moduleVersions, reporter)) {
                maps.add(mapRef);
            }
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
    public MapInfo getMapInfo(AssetPath path) throws IOException {
        var res = getMapInfo(AssetPath.of(), path, 5);

        res.value().properties().put(CACHED_PROPERTY, res.cached);

        return res.value();
    }

    private Result<MapInfo> getMapInfo(AssetPath root, AssetPath path, final int maxLinkDepth) throws IOException {
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

        var info = getMapInfo(base, AssetPath.of(target), maxLinkDepth - 1);
        info.value().merge(props);

        return new Result<>(info.value(), res.cached() && info.cached());
    }

    @Override
    public InputStream open(AssetPath path, AssetRequestOptions options) throws IOException {
        return assetRepository.getStream(path, options).resource();
    }

    @Override
    public Iterable<URI> getUris(AssetPath path, AssetRequestOptions options) {
        return Iterables.transform(
                assetRepository.getUris(path, options),
                res -> res != null ? res.resource() : null
        );
    }

    private record Result<T>(T value, boolean cached) {}
}
