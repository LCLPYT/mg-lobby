package work.lclpnet.activity.manager;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

class ActivityManagerContainer {

    private final ServiceLoader<ActivityManagerProvider> serviceLoader;
    private ActivityManager activityManager = null;

    private ActivityManagerContainer() {
        serviceLoader = ServiceLoader.load(ActivityManagerProvider.class, getClass().getClassLoader());
        load();
    }

    public void load() {
        ActivityManagerProvider provider = serviceLoader.findFirst()
                .orElseThrow(() -> new NoSuchElementException("No ActivityManagerProvider found"));

        activityManager = provider.create();
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }

    static final class Holder {
        static final ActivityManagerContainer instance = new ActivityManagerContainer();
    }
}
