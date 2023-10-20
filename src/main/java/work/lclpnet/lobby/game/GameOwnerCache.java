package work.lclpnet.lobby.game;

import work.lclpnet.mplugins.ext.PluginUnloader;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A cache of {@link GameOwner}s.
 * Should only be created once in the plugin lifecycle.
 */
public class GameOwnerCache {

    private final Map<PluginUnloader, GameOwner> owners = new WeakHashMap<>();

    @Nonnull
    public GameOwner getOwner(PluginUnloader owner) {
        return owners.computeIfAbsent(owner, pluginUnloader -> {
            GameOwner gameOwner = new GameOwner();
            pluginUnloader.registerUnloadable(gameOwner);
            return gameOwner;
        });
    }
}
