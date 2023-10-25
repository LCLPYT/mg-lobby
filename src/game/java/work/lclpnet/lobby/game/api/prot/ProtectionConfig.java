package work.lclpnet.lobby.game.api.prot;

import work.lclpnet.lobby.game.impl.prot.ProtectionTypes;

import javax.annotation.Nullable;

public interface ProtectionConfig {

    <T> void allow(ProtectionType<T> type);

    <T> void disallow(ProtectionType<T> type);

    <T> void allow(ProtectionType<T> type, T scope);

    <T> void disallow(ProtectionType<T> type, T scope);

    <T> boolean hasRestrictions(ProtectionType<T> type);

    @Nullable
    <T> T getAllowedScope(ProtectionType<T> type);

    @Nullable
    <T> T getDisallowedScope(ProtectionType<T> type);

    default void allow(ProtectionType<?>... types) {
        for (ProtectionType<?> type : types) {
            allow(type);
        }
    }

    default void disallow(ProtectionType<?>... types) {
        for (ProtectionType<?> type : types) {
            disallow(type);
        }
    }

    @SuppressWarnings("unchecked")
    default <T> void allow(T scope, ProtectionType<T>... types) {
        for (ProtectionType<T> type : types) {
            allow(type, scope);
        }
    }

    @SuppressWarnings("unchecked")
    default <T> void disallow(T scope, ProtectionType<T>... types) {
        for (ProtectionType<T> type : types) {
            disallow(type, scope);
        }
    }

    /**
     * Disallows all builtin protection types from {@link ProtectionTypes}.
     */
    default void disallowAll() {
        disallow(ProtectionTypes.getTypes()
                .toArray(ProtectionType[]::new));
    }
}
