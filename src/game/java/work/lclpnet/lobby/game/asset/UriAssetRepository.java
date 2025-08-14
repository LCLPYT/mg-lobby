package work.lclpnet.lobby.game.asset;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import work.lclpnet.lobby.game.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

public class UriAssetRepository implements AssetRepository {

    private final URI root;
    private final Logger logger;

    public UriAssetRepository(URI root, Logger logger) {
        this.root = root;
        this.logger = logger;
    }

    @Override
    public Iterable<AssetUriResource> getUris(AssetPath path, AssetRequestOptions options) {
        try {
            URI uri = uri(path);
            AssetUriResource res = new AssetUriResource(uri, false);

            return () -> Iterators.singletonIterator(res);
        } catch (IOException e) {
            logger.debug("Failed to get uri for asset '{}'", path, e);
            return Collections::emptyIterator;
        }
    }

    @Override
    public AssetStreamResource getStream(AssetPath path, AssetRequestOptions options) throws IOException {
        URI uri = uri(path);
        URL url = uri.toURL();
        InputStream in = url.openStream();

        return new AssetStreamResource(in, false);
    }

    public @NotNull URI uri(AssetPath path) throws IOException {
        String encodedPath = AssetPath.of(Arrays.stream(path.segments())
                .map(FileUtil::encodeURIComponent)
                .toArray(String[]::new))
                .toString();

        URI uri = root.resolve(encodedPath);

        if (!uri.getPath().startsWith(root.getPath())) {
            throw new IOException("Path outside of repository");
        }

        return uri;
    }

    @Override
    public String toString() {
        return "UriAssetRepository{root=%s}".formatted(root);
    }
}
