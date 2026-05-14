package work.lclpnet.game.api.prot;

public interface Protection<T> {

    /**
     * Gets the scope that encompasses everything.
     * @return The global scope.
     */
    T getGlobalScope();

    /**
     * Get the resulting scope that is the difference between the exclude and the include scope.
     * @param exclude The exclude scope.
     * @param include The include scope.
     * @return The resulting (difference) scope.
     */
    T getCombinedExcludeScope(T exclude, T include);

    default void allow(ProtectionConfig config, T scope) {
        config.allow(this, scope);
    }

    default void disallow(ProtectionConfig config, T scope) {
        config.disallow(this, scope);
    }

    default void allow(ProtectionConfig config) {
        config.allow(this);
    }

    default void disallow(ProtectionConfig config) {
        config.disallow(this);
    }

    default boolean hasRestrictions(ProtectionConfig config) {
        return config.hasRestrictions(this);
    }
}
