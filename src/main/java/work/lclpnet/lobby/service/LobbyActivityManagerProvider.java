package work.lclpnet.lobby.service;

import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.activity.manager.ActivityManagerProvider;

public class LobbyActivityManagerProvider implements ActivityManagerProvider {

    @Override
    public ActivityManager create() {
        return new LobbyActivityManager();
    }
}
