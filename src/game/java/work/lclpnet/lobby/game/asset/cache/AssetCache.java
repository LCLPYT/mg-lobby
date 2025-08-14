package work.lclpnet.lobby.game.asset.cache;

import org.slf4j.Logger;
import work.lclpnet.kibu.assets.OsUtil;
import work.lclpnet.lobby.game.asset.AssetPath;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Optional;

import static java.nio.file.Files.createDirectories;

public class AssetCache implements AutoCloseable {

    private final CacheIndex index;
    private final Path root;
    private final Logger logger;

    public AssetCache(CacheIndex index, Path root, Logger logger) {
        this.index = index;
        this.root = root;
        this.logger = logger;
    }

    public Optional<Path> getCached(AssetPath path) {
        if (path.isEmpty()) {
            return Optional.empty();
        }

        if (index.isEntryInvalid(path.toString())) {
            return Optional.empty();
        }

        return Optional.of(root.resolve(path.toPath()));
    }

    public Path cache(AssetPath path, InputStream in, int ttlSeconds) throws IOException {
        validateNotEmpty(path);

        Path localPath = root.resolve(path.toPath());

        createDirectories(localPath.getParent());

        try (var out = Files.newOutputStream(localPath)) {
            in.transferTo(out);
        }

        index.updateEntry(path.toString(), ttlSeconds);

        return localPath;
    }

    public Optional<Path> cache(AssetPath path, URI uri, int ttlSeconds) throws IOException {
        // only cache remote files
        if (uri.getHost() == null) {
            return Optional.empty();
        }

        URL url;

        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            logger.error("Failed to cache resource: {} cannot be converted to a URL", uri, e);
            return Optional.empty();
        }

        // only cache remote files
        if ("file".equalsIgnoreCase(url.getProtocol())) {
            return Optional.empty();
        }

        URLConnection connection = url.openConnection();

        try (var in = connection.getInputStream()) {
            return Optional.of(cache(path, in, ttlSeconds));
        }
    }

    private void validateNotEmpty(AssetPath path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Cannot cache empty asset path");
        }
    }

    public void invalidate(AssetPath path) {
        index.invalidate(path.toString());
    }

    @Override
    public void close() throws Exception {
        index.close();
    }

    public static AssetCache createUserCache(String type, Logger logger) throws IOException {
        var assetCacheRoot = OsUtil.getCacheDir()
                .resolve("mc-game-commons")
                .resolve("assets")
                .resolve(type);

        return create(assetCacheRoot, logger);
    }

    public static AssetCache create(Path root, Logger logger) throws IOException {
        Files.createDirectories(root);

        Path indexPath = root.resolve("index.sqlite");

        CacheIndex index;

        try {
            index = SqliteCacheIndex.createSqliteIndex(indexPath, logger);
        } catch (SQLException e) {
            logger.error("Failed to create SQLite cache index, cache will not be used", e);
            index = VoidCacheIndex.getInstance();
        }

        return new AssetCache(index, root, logger);
    }
}
