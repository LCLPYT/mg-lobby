package work.lclpnet.lobby.game.map.cache;

public class VoidCacheIndex implements CacheIndex {

    @Override
    public void close() {}

    @Override
    public boolean hasValidEntry(String path, int ttlSeconds) {
        return false;
    }
}
