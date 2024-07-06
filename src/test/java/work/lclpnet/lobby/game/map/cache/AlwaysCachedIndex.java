package work.lclpnet.lobby.game.map.cache;

class AlwaysCachedIndex implements CacheIndex {

    public static final CacheIndex INSTANCE = new AlwaysCachedIndex();

    private AlwaysCachedIndex() {}

    @Override
    public boolean isEntryInvalid(String path) {
        return false;
    }

    @Override
    public void updateEntry(String path, int ttlSeconds) {}

    @Override
    public void close() {}
}
