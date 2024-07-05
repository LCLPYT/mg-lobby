package work.lclpnet.lobby.game.map.cache;

import java.io.Closeable;

public interface CacheIndex extends Closeable {

    boolean hasValidEntry(String path, int ttlSeconds);
}
