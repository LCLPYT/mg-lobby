package work.lclpnet.lobby.game.asset.cache;

import work.lclpnet.lobby.game.asset.AssetPath;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.file.Files.createDirectories;

public class AssetCache {

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

    public Path cache(AssetPath path, InputStream in) throws IOException {
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Cannot cache empty asset path");
        }

        Path localPath = root.resolve(path.toPath());

        createDirectories(localPath.getParent());

        try (var out = Files.newOutputStream(localPath)) {
            in.transferTo(out);
        }

        return localPath;
    }
}
