package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.GenericScope;

public class GenericProtectionType<T> implements ProtectionType<GenericScope<T>> {

    @Override
    public GenericScope<T> getGlobalScope() {
        return arg -> true;
    }

    @Override
    public GenericScope<T> getResultingScope(GenericScope<T> disallowed, GenericScope<T> allowed) {
        return arg -> disallowed.isWithinScope(arg) && !allowed.isWithinScope(arg);
    }
}
