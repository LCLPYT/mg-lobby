package work.lclpnet.lobby.game.api.prot;

public interface Scope<T> {

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
    T getResultingScope(T exclude, T include);

    default void allow(ProtectionConfig config, T scope) {
        config.allow(this, scope);
    }

    default void disallow(ProtectionConfig config, T scope) {
        config.disallow(this, scope);
    }
}
