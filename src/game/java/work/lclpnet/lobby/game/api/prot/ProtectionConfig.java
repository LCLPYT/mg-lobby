package work.lclpnet.lobby.game.api.prot;

import org.jetbrains.annotations.Nullable;
import work.lclpnet.lobby.game.impl.prot.ProtectionTypes;

public interface ProtectionConfig {

    <T> void allow(Scope<T> type);

    <T> void disallow(Scope<T> type);

    <T> void allow(Scope<T> type, T scope);

    <T> void disallow(Scope<T> type, T scope);

    <T> boolean hasRestrictions(Scope<T> type);

    @Nullable
    <T> T getAllowedScope(Scope<T> type);

    @Nullable
    <T> T getDisallowedScope(Scope<T> type);

    default void allow(Scope<?>... types) {
        for (Scope<?> type : types) {
            allow(type);
        }
    }

    default void disallow(Scope<?>... types) {
        for (Scope<?> type : types) {
            disallow(type);
        }
    }

    /**
     * Allows all builtin protection types from {@link ProtectionTypes}.
     */
    default void allowAll() {
        for (Scope<?> type : ProtectionTypes.getTypes()) {
            allow(type);
        }
    }

    /**
     * Disallows all builtin protection types from {@link ProtectionTypes}.
     */
    default void disallowAll() {
        for (Scope<?> type : ProtectionTypes.getTypes()) {
            disallow(type);
        }
    }
}
