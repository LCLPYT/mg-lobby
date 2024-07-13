package work.lclpnet.lobby.game.map.cache;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import work.lclpnet.lobby.game.map.MapInfo;
import work.lclpnet.lobby.game.map.MapRef;
import work.lclpnet.lobby.game.map.UriMapRepository;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;

public class MapCache implements Closeable {

    private final CacheIndex index;
    private final UriMapRepository cacheRepository;
    private final int ttlSeconds;
    private final Logger logger;

    public MapCache(CacheIndex index, UriMapRepository cacheRepository, int ttlSeconds, Logger logger) {
        this.ttlSeconds = ttlSeconds;
        this.logger = logger;
        this.index = index;
        this.cacheRepository = cacheRepository;
    }

    @Nullable
    public Collection<MapRef> getCachedMapList(String path) {
        String entry = path + "/index.json";

        if (index.isEntryInvalid(entry)) {
            return null;
        }

        try {
            return cacheRepository.getMapList(path);
        } catch (IOException e) {
            logger.error("Failed to get cached map list for {}", path, e);
            return null;
        }
    }

    @Nullable
    public MapInfo getCachedMapInfo(String path) {
        String entry = path + "/map.json";

        if (index.isEntryInvalid(entry)) {
            return null;
        }

        MapInfo mapInfo;

        try {
            mapInfo = cacheRepository.getMapInfo(path);
        } catch (IOException e) {
            logger.error("Failed to get cached map info for {}", path, e);
            return null;
        }

        return mapInfo;
    }

    @Nullable
    public Path getCachedResource(String path, String resource) {
        String entry = getResourceEntry(path, resource);

        if (index.isEntryInvalid(entry)) {
            return null;
        }

        var res = cacheRepository.getResource(path, resource);

        if (res.isEmpty()) {
            return null;
        }

        URI uri = res.get();
        Path resourcePath;

        try {
            resourcePath = Path.of(uri);
        } catch (RuntimeException e) {
            logger.error("Failed to resolve path from cache uri {}", uri);
            return null;
        }

        if (Files.isRegularFile(resourcePath)) {
            return resourcePath;
        }

        return null;
    }

    @Override
    public void close() throws IOException {
        index.close();
    }

    @Nullable
    public Path getCachePath(String path) {
        URI root = cacheRepository.getRoot();
        URI cacheUri = root.resolve(path);

        if (cacheUri.getPath().startsWith(root.getPath())) {
            return Path.of(cacheUri);
        }

        return null;
    }

    public void cacheMapInfo(String path, MapInfo info) {
        String entry = path + "/map.json";
        Path cachePath = getCachePath(entry);

        if (cachePath == null) {
            logger.warn("Failed to cache map info: path {} escapes the cache directory", path);
            return;
        }

        JSONObject json = new JSONObject();
        info.toJson(json);

        try {
            Files.createDirectories(cachePath.getParent());

            Files.writeString(cachePath, json.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("Failed to cache map info", e);
            return;
        }

        index.updateEntry(entry, ttlSeconds);
    }

    public void cacheMapList(String path, Collection<MapRef> mapList) {
        String entry = path + "/index.json";
        Path cachePath = getCachePath(entry);

        if (cachePath == null) {
            logger.warn("Failed to cache map list: path {} escapes the cache directory", path);
            return;
        }

        JSONObject json = new JSONObject();
        JSONArray maps = new JSONArray();

        for (MapRef ref : mapList) {
            JSONObject refJson = new JSONObject();
            ref.toJson(refJson);

            maps.put(refJson);
        }

        json.put("maps", maps);

        try {
            Files.createDirectories(cachePath.getParent());

            Files.writeString(cachePath, json.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("Failed to cache map list", e);
            return;
        }

        index.updateEntry(entry, ttlSeconds);
    }

    @Nullable
    public Path cacheResource(String path, String resource, URI uri) {
        // only cache remote files
        if (uri.getHost() == null) {
            return null;
        }

        URL url;

        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            logger.error("Failed to cache resource: {} cannot be converted to a URL", uri, e);
            return null;
        }

        // only cache remote files
        if ("file".equalsIgnoreCase(url.getProtocol())) {
            return null;
        }

        String entry = getResourceEntry(path, resource);
        Path cachePath = getCachePath(entry);

        if (cachePath == null) {
            logger.warn("Failed to cache resource: path {} escapes the cache directory", entry);
            return null;
        }

        try {
            Files.createDirectories(cachePath.getParent());

            URLConnection connection = url.openConnection();

            try (var in = connection.getInputStream()) {
                Files.copy(in, cachePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            logger.error("Failed to cache resource {}", cachePath, e);
            return null;
        }

        index.updateEntry(entry, ttlSeconds);

        return cachePath;
    }

    private String getResourceEntry(String path, String resource) {
        return URI.create(path + "/").resolve(resource).toString();
    }

    public void invalidateSource(MapInfo info) {
        String source = info.getSource();

        if (source == null) return;

        String entry = getResourceEntry(info.target() + "/", source);

        invalidate(entry);
    }

    public void invalidate(String entry) {
        if (getCachePath(entry) == null) {
            logger.error("Cannot invalidate path outside the cache directory: {}", entry);
            return;
        }

        index.invalidate(entry);
    }

    public static MapCache createUserCache(Logger logger) throws IOException {
        String prop = System.getProperty("user.home");

        Objects.requireNonNull(prop, "Property user.home doesn't exist");

        Path userHome = Path.of(prop);
        Path root = userHome.resolve(".maps");

        return createCache(root, logger);
    }

    public static MapCache createCache(Path root, Logger logger) throws IOException {
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }

        Path indexPath = root.resolve("index.sqlite");

        CacheIndex index;

        try {
            index = SqliteCacheIndex.createSqliteIndex(indexPath, logger);
        } catch (SQLException e) {
            logger.error("Failed to create SQLite cache index, cache will not be used", e);
            index = VoidCacheIndex.getInstance();
        }

        URI rootUri = root.toUri();

        UriMapRepository repository = new UriMapRepository(rootUri, logger);

        return new MapCache(index, repository, 3600, logger);
    }
}
