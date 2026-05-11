package work.lclpnet.lobby.game.impl.prot;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import work.lclpnet.lobby.game.api.prot.ProtectionConfig;
import work.lclpnet.lobby.game.api.prot.Scope;

import java.util.Map;

public class MutableProtectionConfig implements ProtectionConfig {

    private final Map<Scope<?>, Object> allow = new Object2ObjectOpenHashMap<>();
    private final Map<Scope<?>, Object> disallow = new Object2ObjectOpenHashMap<>();

    @Override
    public <T> void allow(Scope<T> type) {
        disallow.remove(type);
    }

    @Override
    public <T> void disallow(Scope<T> type) {
        disallow.put(type, type.getGlobalScope());
        allow.remove(type);
    }

    @Override
    public <T> void allow(Scope<T> type, T scope) {
        allow.put(type, scope);
    }

    @Override
    public <T> void disallow(Scope<T> type, T scope) {
        disallow.put(type, scope);
    }

    @Override
    public <T> boolean hasRestrictions(Scope<T> type) {
        return disallow.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAllowedScope(Scope<T> type) {
        Object scope = allow.get(type);

        if (scope == null) {
            return null;
        }

        return (T) scope;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getDisallowedScope(Scope<T> type) {
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
