package work.lclpnet.game.impl.prot;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import work.lclpnet.game.api.prot.Protection;
import work.lclpnet.game.api.prot.ProtectionConfig;

import java.util.Map;

public class MutableProtectionConfig implements ProtectionConfig {

    private final Map<Protection<?>, Object> allow = new Object2ObjectOpenHashMap<>();
    private final Map<Protection<?>, Object> disallow = new Object2ObjectOpenHashMap<>();

    @Override
    public <T> void allow(Protection<T> type) {
        disallow.remove(type);
    }

    @Override
    public <T> void disallow(Protection<T> type) {
        disallow.put(type, type.getGlobalScope());
        allow.remove(type);
    }

    @Override
    public <T> void allow(Protection<T> type, T scope) {
        allow.put(type, scope);
    }

    @Override
    public <T> void disallow(Protection<T> type, T scope) {
        disallow.put(type, scope);
    }

    @Override
    public <T> boolean hasRestrictions(Protection<T> type) {
        return disallow.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAllowedScope(Protection<T> type) {
        Object scope = allow.get(type);

        if (scope == null) {
            return null;
        }

        return (T) scope;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getDisallowedScope(Protection<T> type) {
        Object scope = disallow.get(type);

        if (scope == null) {
            return null;
        }

        return (T) scope;
    }

    @Override
    public void allowAll() {
        disallow.clear();
    }
}
