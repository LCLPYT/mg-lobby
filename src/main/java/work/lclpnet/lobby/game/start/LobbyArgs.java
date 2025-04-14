package work.lclpnet.lobby.game.start;

import work.lclpnet.activity.Activity;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.lobby.activity.GameStartingActivity;
import work.lclpnet.lobby.activity.LobbyActivity;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LobbyArgs implements GameStarter.Args {

    private final ActivityManager childActivity;
    private final LobbyGameConfigurator configurator;
    private Supplier<GameStartingActivity> startingSupplier = null;

    public LobbyArgs(ActivityManager childActivity, LobbyGameConfigurator configurator) {
        this.childActivity = childActivity;
        this.configurator = configurator;
    }

    @Override
    public void startChildActivity(Activity activity) {
        childActivity.startActivity(activity);
    }

    @Override
    public void stopChildActivity() {
        childActivity.stop();
    }

    public void injectStartingSupplier(Supplier<GameStartingActivity> supplier) {
        this.startingSupplier = supplier;
    }

    public GameStartingActivity createGameStartingActivity() {
        return startingSupplier.get();
    }

    public void configureLobby(Consumer<LobbyActivity> ifActive) {
        configurator.configure(ifActive);
    }
}
