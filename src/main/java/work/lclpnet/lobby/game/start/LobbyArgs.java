package work.lclpnet.lobby.game.start;

import work.lclpnet.activity.Activity;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.kibu.plugin.ext.PluginContext;
import work.lclpnet.lobby.activity.GameStartingActivity;
import work.lclpnet.lobby.activity.LobbyActivity;
import work.lclpnet.lobby.game.api.GameStarter;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LobbyArgs implements GameStarter.Args {

    private final PluginContext pluginContext;
    private final ActivityManager childActivity;
    private final LobbyGameConfigurator configurator;
    private Supplier<GameStartingActivity> startingSupplier = null;

    public LobbyArgs(PluginContext pluginContext, ActivityManager childActivity, LobbyGameConfigurator configurator) {
        this.pluginContext = pluginContext;
        this.childActivity = childActivity;
        this.configurator = configurator;
    }

    @Override
    public PluginContext getPluginContext() {
        return pluginContext;
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
