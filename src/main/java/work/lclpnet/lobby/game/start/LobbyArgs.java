package work.lclpnet.lobby.game.start;

import work.lclpnet.activity.Activity;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.lobby.activity.LobbyActivity;

import java.util.function.Consumer;

public class LobbyArgs {

    private final ActivityManager childActivity;
    private final LobbyGameConfigurator configurator;

    public LobbyArgs(ActivityManager childActivity, LobbyGameConfigurator configurator) {
        this.childActivity = childActivity;
        this.configurator = configurator;
    }

    public void startChildActivity(Activity activity) {
        childActivity.startActivity(activity);
    }

    public void stopChildActivity() {
        childActivity.stop();
    }

    public void configureLobby(Consumer<LobbyActivity> ifActive) {
        configurator.configure(ifActive);
    }
}
