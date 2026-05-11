package work.lclpnet.lobby.game.impl.prot.scope;

import work.lclpnet.lobby.game.api.prot.Scope;

public class GenericScope<T> implements Scope<GenericScope.Check<T>> {

    @Override
    public Check<T> getGlobalScope() {
        return _ -> true;
    }

    @Override
    public Check<T> getResultingScope(Check<T> exclude, Check<T> include) {
        return arg -> exclude.isWithinScope(arg) && !include.isWithinScope(arg);
    }

    public interface Check<T> {

        boolean isWithinScope(T arg);
    }
}
