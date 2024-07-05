package work.lclpnet.lobby.game.map.cache;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import work.lclpnet.lobby.game.map.GameMap;
import work.lclpnet.lobby.game.map.MapInfo;
import work.lclpnet.lobby.game.map.MapRef;
import work.lclpnet.lobby.game.map.UriMapRepository;
import work.lclpnet.lobby.game.util.FileUtil;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;

public class MapCache implements Closeable {

    private final CacheIndex index;
    private final UriMapRepository repository;
    private final int ttlSeconds;
    private final Logger logger;

    public MapCache(CacheIndex index, UriMapRepository repository, int ttlSeconds, Logger logger) {
        this.ttlSeconds = ttlSeconds;
        this.logger = logger;
        this.index = index;
        this.repository = repository;
    }

    @Nullable
    public Collection<MapRef> getCachedMapList(String path) {
        String entry = Path.of(path).resolve("index.json").toString();

        if (index.isEntryInvalid(entry, ttlSeconds)) {
            return null;
        }

        try {
            return repository.getMapList(path);
        } catch (IOException e) {
            logger.error("Failed to get cached map list for {}", path, e);
            return null;
        }
    }

    @Nullable
    public MapInfo getCachedMapInfo(String path) {
        String entry = Path.of(path).resolve("map.json").toString();

        if (index.isEntryInvalid(entry, ttlSeconds)) {
            return null;
        }

        MapInfo mapInfo;

        try {
            mapInfo = repository.getMapInfo(path);
        } catch (IOException e) {
            logger.error("Failed to get cached map info for {}", path, e);
            return null;
        }

        MapInfo modified = withCachedSourceIfAvailable(path, mapInfo);

        if (modified != null) {
            return modified;
        }

        return mapInfo;
    }

    @Nullable
    private MapInfo withCachedSourceIfAvailable(String path, MapInfo mapInfo) {
        Path cachePath = getCachedMapSource(path, mapInfo);

        if (cachePath == null) return null;

        String cacheFileName = cachePath.getFileName().toString();

        return mapInfo.withSource(cacheFileName);
    }

    @Override
    public void close() throws IOException {
        index.close();
    }

    @Nullable
    public Path getCachePath(String path) {
        Path root = Path.of(repository.getRoot());
        Path cachePath = root.resolve(path);

        if (cachePath.startsWith(root)) {
            return cachePath;
        }

        return null;
    }

    public void cacheMapInfo(String path, MapInfo info) {
        Path cachePath = getCachePath(path + "/map.json");

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
        }
    }

    public void cacheMapList(String path, Collection<MapRef> mapList) {
        Path cachePath = getCachePath(path + "/index.json");

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
        }
    }

    @Nullable
    public Path getCachedMapSource(GameMap map) {
        if (!map.hasProperty("target", String.class)) {
            return null;
        }

        String path = map.getProperty("target");

        // try to infer source directly from the map instance
        Object source = map.getProperty("source");

        if (source instanceof URI sourceUri) {
            Path cachedSource = getCachedMapSource(path, sourceUri);

            if (cachedSource != null) {
                return cachedSource;
            }
        }

        // try to find cached map info
        MapInfo mapInfo = getCachedMapInfo(path);

        if (mapInfo == null) return null;

        // try to find cached map source
        return getCachedMapSource(path, mapInfo);
    }

    @Nullable
    private Path getCachedMapSource(String path, URI source) {
        String sourcePath = source.getPath();

        if (sourcePath == null) return null;

        String fileName;

        try {
            fileName = Path.of(sourcePath).getFileName().toString();
        } catch (InvalidPathException ignored) {
            return null;
        }

        return getCachedMapSource(path, fileName);
    }

    @Nullable
    private Path getCachedMapSource(String path, MapInfo mapInfo) {
        String source = mapInfo.getSource();

        if (source == null) return null;

        var uri = FileUtil.getUri(mapInfo.uri(), source);

        if (uri.isEmpty()) return null;

        String sourcePath = uri.get().getPath();

        if (sourcePath == null) return null;

        String cacheFileName;

        try {
            cacheFileName = Path.of(sourcePath).getFileName().toString();
        } catch (InvalidPathException e) {
            logger.error("Failed to determine map source path file name", e);
            return null;
        }

        return getCachedMapSource(path, cacheFileName);
    }

    @Nullable
    private Path getCachedMapSource(String path, String cacheFileName) {
        Path cachePath = getCachePath(path + "/" + cacheFileName);

        if (cachePath == null || !Files.exists(cachePath)) return null;

        return cachePath;
    }

    @Nullable
    public Path cacheMapSource(GameMap map, URL source) {
        if (!map.hasProperty("target", String.class)) {
            return null;
        }

        String path = map.getProperty("target");
        String sourcePath = source.getPath();

        if (sourcePath == null) {
            logger.warn("Failed to cache map source: Path of {} is undefined", source);
            return null;
        }

        String fileName;

        try {
            fileName = Path.of(sourcePath).getFileName().toString();
        } catch (InvalidPathException e) {
            logger.warn("Could not determine map source file name of {}", source, e);
            return null;
        }

        // try to copy the map to the cache first
        Path cachePath = getCachePath(path + "/" + fileName);

        if (cachePath == null) {
            logger.warn("Could not determine cache path for {}", path + "/" + fileName);
            return null;
        }

        // copy to cache
        try {
            Files.createDirectories(cachePath.getParent());

            URLConnection connection = source.openConnection();

            try (var in = connection.getInputStream()) {
                Files.copy(in, cachePath);
            }
        } catch (IOException e) {
            logger.error("Failed to cache map source for {}", cachePath, e);
            return null;
        }

        return cachePath;
    }

    public static MapCache createUserCache(Logger logger) throws IOException {
        String prop = System.getProperty("user.home");

        Objects.requireNonNull(prop, "Property user.home doesn't exist");

        Path userHome = Path.of(prop);
        Path root = userHome.resolve(".maps");

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
