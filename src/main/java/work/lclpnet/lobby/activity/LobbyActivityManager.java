package work.lclpnet.lobby.activity;

import work.lclpnet.activity.Activity;
import work.lclpnet.activity.manager.ActivityManager;

public class LobbyActivityManager implements ActivityManager {

    private Activity currentActivity = null;

    @Override
    public void startActivity(Activity activity) {
        synchronized (this) {
            if (currentActivity != null) {
                currentActivity.stop();
            }

            currentActivity = activity;
            currentActivity.start();
        }
    }

    @Override
    public void stop() {
        synchronized (this) {
            if (currentActivity == null) return;

            currentActivity.stop();
            currentActivity = null;
        }
    }
}
