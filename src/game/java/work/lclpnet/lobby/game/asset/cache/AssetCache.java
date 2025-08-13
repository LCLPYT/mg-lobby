package work.lclpnet.lobby.game.asset.cache;

import org.slf4j.Logger;
import work.lclpnet.kibu.assets.OsUtil;
import work.lclpnet.lobby.game.asset.AssetPath;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Optional;

import static java.nio.file.Files.createDirectories;

public class AssetCache implements AutoCloseable {

    private final CacheIndex index;
    private final Path root;

    public AssetCache(CacheIndex index, Path root) {
        this.index = index;
        this.root = root;
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
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Cannot cache empty asset path");
        }

        Path localPath = root.resolve(path.toPath());

        createDirectories(localPath.getParent());

        try (var out = Files.newOutputStream(localPath)) {
            in.transferTo(out);
        }

        index.updateEntry(path.toString(), ttlSeconds);

        return localPath;
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

        return new AssetCache(index, root);
    }
}
