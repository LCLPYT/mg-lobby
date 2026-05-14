package work.lclpnet.game.api.prot;

import org.jetbrains.annotations.Nullable;
import work.lclpnet.game.impl.prot.ProtectionTypes;

public interface ProtectionConfig {

    <T> void allow(Protection<T> type);

    <T> void disallow(Protection<T> type);

    <T> void allow(Protection<T> type, T scope);

    <T> void disallow(Protection<T> type, T scope);

    <T> boolean hasRestrictions(Protection<T> type);

    @Nullable
    <T> T getAllowedScope(Protection<T> type);

    @Nullable
    <T> T getDisallowedScope(Protection<T> type);

    default void allow(Protection<?>... types) {
        for (Protection<?> type : types) {
            allow(type);
        }
    }

    default void disallow(Protection<?>... types) {
        for (Protection<?> type : types) {
            disallow(type);
        }
    }

    /**
     * Allows all builtin protection types from {@link ProtectionTypes}.
     */
    default void allowAll() {
        for (Protection<?> type : ProtectionTypes.getTypes()) {
            allow(type);
        }
    }

    /**
     * Disallows all builtin protection types from {@link ProtectionTypes}.
     */
    default void disallowAll() {
        for (Protection<?> type : ProtectionTypes.getTypes()) {
            disallow(type);
        }
    }
}
