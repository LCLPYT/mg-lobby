package work.lclpnet.activity.manager;

import work.lclpnet.activity.Activity;

public interface ActivityManager {

    void startActivity(Activity activity);

    void stop();

    static ActivityManager getInstance() {
        return ActivityManagerContainer.Holder.instance.getActivityManager();
    }
}
