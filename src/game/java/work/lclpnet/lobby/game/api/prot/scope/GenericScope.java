package work.lclpnet.lobby.game.api.prot.scope;

public interface GenericScope<T> {

    boolean isWithinScope(T arg);
}
