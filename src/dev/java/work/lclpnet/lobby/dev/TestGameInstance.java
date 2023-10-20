package work.lclpnet.lobby.dev;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.kibu.plugin.ext.PluginContext;
import work.lclpnet.lobby.LobbyPlugin;
import work.lclpnet.lobby.game.GameEnvironment;
import work.lclpnet.lobby.game.GameInstance;
import work.lclpnet.lobby.game.GameStarter;
import work.lclpnet.lobby.game.start.ConditionGameStarter;

import java.util.function.BooleanSupplier;

public class TestGameInstance implements GameInstance {

    private final GameEnvironment environment;

    public TestGameInstance(GameEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public GameStarter createStarter(GameStarter.Args args, GameStarter.Callback onStart) {
        BooleanSupplier condition = () -> !PlayerLookup.all(environment.getServer()).isEmpty();

        return new ConditionGameStarter(condition, args, onStart, environment);
    }

    @Override
    public void start() {
        System.out.println("The test game was started! (will end in 10 seconds)");

        // normally, you would use static getInstance() of your plugin
        PluginContext context = LobbyPlugin.getInstance();

        TestGameActivity activity = new TestGameActivity(context);

        ActivityManager.getInstance().startActivity(activity);

        environment.getSchedulerStack().timeout(() -> {
            System.out.println("The test game has ended!");
            environment.getFinisher().finishGame();
        }, 200);
    }
}
