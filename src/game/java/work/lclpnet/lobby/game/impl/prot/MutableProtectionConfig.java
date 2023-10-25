package work.lclpnet.lobby.game.impl.prot;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import work.lclpnet.lobby.game.api.prot.ProtectionConfig;
import work.lclpnet.lobby.game.api.prot.ProtectionType;

import java.util.Map;

public class MutableProtectionConfig implements ProtectionConfig {

    private final Map<ProtectionType<?>, Object> allow = new Object2ObjectOpenHashMap<>();
    private final Map<ProtectionType<?>, Object> disallow = new Object2ObjectOpenHashMap<>();

    @Override
    public <T> void allow(ProtectionType<T> type) {
        disallow.remove(type);
    }

    @Override
    public <T> void disallow(ProtectionType<T> type) {
        disallow.put(type, type.getGlobalScope());
    }

    @Override
    public <T> void allow(ProtectionType<T> type, T scope) {
        allow.put(type, scope);
    }

    @Override
    public <T> void disallow(ProtectionType<T> type, T scope) {
        disallow.put(type, scope);
    }

    @Override
    public <T> boolean hasRestrictions(ProtectionType<T> type) {
        return disallow.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAllowedScope(ProtectionType<T> type) {
        Object scope = allow.get(type);

        if (scope == null) {
            return null;
        }

        return (T) scope;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getDisallowedScope(ProtectionType<T> type) {
        Object scope = disallow.get(type);

        if (scope == null) {
            return null;
        }

        return (T) scope;
    }

    @SafeVarargs
    @Override
    public final <T> void allow(T scope, ProtectionType<T>... types) {
        ProtectionConfig.super.allow(scope, types);
    }

    @SafeVarargs
    @Override
    public final <T> void disallow(T scope, ProtectionType<T>... types) {
        ProtectionConfig.super.disallow(scope, types);
    }

    @Override
    public void allowAll() {
        disallow.clear();
    }
}
