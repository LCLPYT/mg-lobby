package work.lclpnet.lobby.game;

import work.lclpnet.lobby.game.api.GameFinisher;
import work.lclpnet.mplugins.ext.Unloadable;

public class GameOwner implements Unloadable {

    // Hack: make sure the GameFinisher.Reason class is loaded when this class is loaded.
    // Otherwise, when unloading the plugin, this can throw a NoClassDefFound error if no game ever ended before.
    private static final GameFinisher.Reason REASON = GameFinisher.Reason.UNLOADED;
    private volatile GameFinisher finisher = null;

    public void setFinisher(GameFinisher finisher) {
        synchronized (this) {
            this.finisher = finisher;
        }
    }

    @Override
    public void unload() {
        GameFinisher finisher;

        synchronized (this) {
            if (this.finisher == null) return;

            finisher = this.finisher;
            this.finisher = null;
        }

        finisher.finishGame(REASON);
    }

    public void detach() {
        setFinisher(null);
    }
}
