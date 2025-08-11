package work.lclpnet.lobby.game.asset.cache;

import java.io.Closeable;

public interface CacheIndex extends Closeable {

    boolean isEntryInvalid(String path);

    void updateEntry(String path, int ttlSeconds);

    void invalidate(String path);
}
