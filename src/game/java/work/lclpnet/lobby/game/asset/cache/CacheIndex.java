package work.lclpnet.lobby.game.asset.cache;

public interface CacheIndex extends AutoCloseable {

    boolean isEntryInvalid(String path);

    void updateEntry(String path, int ttlSeconds);

    void invalidate(String path);
}
