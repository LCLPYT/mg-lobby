package work.lclpnet.lobby.game.asset.cache;

public class VoidCacheIndex implements CacheIndex {

    private VoidCacheIndex() {}

    @Override
    public void close() {}

    @Override
    public boolean isEntryInvalid(String path) {
        return true;
    }

    @Override
    public void updateEntry(String path, int ttlSeconds) {}

    @Override
    public void invalidate(String path) {}

    public static VoidCacheIndex getInstance() {
        return Holder.INSTANCE;
    }

    // lazy singleton
    private static class Holder {
        private static final VoidCacheIndex INSTANCE = new VoidCacheIndex();
    }
}
