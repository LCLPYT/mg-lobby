package work.lclpnet.lobby.game.impl.prot.scope;

import work.lclpnet.lobby.game.api.prot.Protection;

public class GenericProtection<T> implements Protection<GenericProtection.Scope<T>> {

    @Override
    public Scope<T> getGlobalScope() {
        return _ -> true;
    }

    @Override
    public Scope<T> getCombinedExcludeScope(Scope<T> exclude, Scope<T> include) {
        return arg -> exclude.isWithinScope(arg) && !include.isWithinScope(arg);
    }

    public interface Scope<T> {

        boolean isWithinScope(T arg);
    }
}
