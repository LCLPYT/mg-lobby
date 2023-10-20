package work.lclpnet.lobby.game;

import work.lclpnet.activity.Activity;
import work.lclpnet.kibu.plugin.ext.PluginContext;

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
        PluginContext getPluginContext();

        void startChildActivity(Activity activity);

        void stopChildActivity();
    }
}
