package work.lclpnet.lobby.game.asset;

public record AssetRequestOptions(boolean disableCache) {

    public static final AssetRequestOptions DEFAULT = new AssetRequestOptions(false);
}
