package work.lclpnet.lobby.game.impl.prot.type;

import work.lclpnet.lobby.game.api.prot.ProtectionType;
import work.lclpnet.lobby.game.api.prot.scope.ClickEventScope;

public class ClickEventProtectionType implements ProtectionType<ClickEventScope> {

    public static final ClickEventProtectionType INSTANCE = new ClickEventProtectionType();

    private ClickEventProtectionType() {}

    @Override
    public ClickEventScope getGlobalScope() {
        return event -> true;
    }

    @Override
    public ClickEventScope getResultingScope(ClickEventScope disallowed, ClickEventScope allowed) {
        return event -> disallowed.isWithinScope(event) && !allowed.isWithinScope(event);
    }
}
