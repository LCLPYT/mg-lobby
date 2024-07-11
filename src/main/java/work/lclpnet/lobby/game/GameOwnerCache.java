package work.lclpnet.lobby.game;

import org.jetbrains.annotations.NotNull;
import work.lclpnet.mplugins.ext.PluginUnloader;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * A cache of {@link GameOwner}s.
 * Should only be created once in the plugin lifecycle.
 */
public class GameOwnerCache {

    private final Map<PluginUnloader, GameOwner> owners = new WeakHashMap<>();

    @NotNull
    public GameOwner getOwner(PluginUnloader owner) {
        return owners.computeIfAbsent(owner, pluginUnloader -> {
            GameOwner gameOwner = new GameOwner();
            pluginUnloader.registerUnloadable(gameOwner);
            return gameOwner;
        });
    }
}
