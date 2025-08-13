package work.lclpnet.lobby.game.asset;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public record AssetResult(InputStream resource, boolean cached) implements Closeable {

    @Override
    public void close() throws IOException {
        if (resource != null) {
            resource.close();
        }
    }
}
