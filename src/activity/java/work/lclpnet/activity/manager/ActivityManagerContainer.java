package work.lclpnet.activity.manager;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

class ActivityManagerContainer {

    private final ServiceLoader<ActivityManager> serviceLoader = ServiceLoader.load(ActivityManager.class);
    private ActivityManager activityManager = null;

    private ActivityManagerContainer() {
        load();
    }

    public void load() {
        activityManager = serviceLoader.findFirst().orElseThrow(() -> new NoSuchElementException("No ActivityManager implementation found"));
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }

    static final class Holder {
        static final ActivityManagerContainer instance = new ActivityManagerContainer();
    }
}
