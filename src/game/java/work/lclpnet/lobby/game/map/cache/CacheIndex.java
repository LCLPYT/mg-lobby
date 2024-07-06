package work.lclpnet.lobby.game.map.cache;

import java.io.Closeable;

public interface CacheIndex extends Closeable {

    boolean isEntryInvalid(String path);

    void updateEntry(String path, int ttlSeconds);
}
