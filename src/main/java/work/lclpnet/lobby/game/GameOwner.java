package work.lclpnet.lobby.game;

import work.lclpnet.mplugins.ext.Unloadable;

public class GameOwner implements Unloadable {

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

        finisher.finishGame(GameFinisher.Reason.UNLOADED);
    }

    public void detach() {
        setFinisher(null);
    }
}
