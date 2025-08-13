package work.lclpnet.lobby.game.asset;

import org.jetbrains.annotations.NotNull;
import work.lclpnet.lobby.game.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

public class UriAssetRepository implements AssetRepository {

    private final URI root;

    public UriAssetRepository(URI root) {
        this.root = root;
    }

    @Override
    public AssetResult get(AssetPath path, AssetRequestOptions options) throws IOException {
        URI uri = uri(path);

        if (!uri.getPath().startsWith(root.getPath())) {
            throw new IOException("Path outside of repository");
        }

        URL url = uri.toURL();
        InputStream in = url.openStream();

        return new AssetResult(in, false);
    }

    public @NotNull URI uri(AssetPath path) {
        String encodedPath = AssetPath.of(Arrays.stream(path.segments())
                .map(FileUtil::encodeURIComponent)
                .toArray(String[]::new))
                .toString();

        return root.resolve(encodedPath);
    }

    @Override
    public String toString() {
        return "UriAssetRepository{root=%s}".formatted(root);
    }
}
