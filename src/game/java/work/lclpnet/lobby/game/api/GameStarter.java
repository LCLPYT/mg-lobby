package work.lclpnet.lobby.game.api;

import work.lclpnet.activity.Activity;

public interface GameStarter {

    void start();

    void finish();

    boolean isStarted();

    void setPaused(boolean paused);

    boolean isPaused();

    void destroy();

    interface Callback {
        void start();
    }

    interface Args {

        void startChildActivity(Activity activity);

        void stopChildActivity();
    }
}
