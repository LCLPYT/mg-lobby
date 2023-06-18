package work.lclpnet.lobby.activity;

import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBuilder;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.lobby.game.conf.GameConfig;

public class GameStartingActivity extends ComponentActivity {

    private final GameConfig gameConfig;
    private final Runnable start;

    public GameStartingActivity(PluginContext context, GameConfig gameConfig, Runnable start) {
        super(context);
        this.gameConfig = gameConfig;
        this.start = start;
    }

    @Override
    protected void buildComponents(ComponentBuilder components) {

    }

    @Override
    public void start() {
        super.start();

        System.out.printf("Starting '%s'...%n", gameConfig.title());
    }
}
