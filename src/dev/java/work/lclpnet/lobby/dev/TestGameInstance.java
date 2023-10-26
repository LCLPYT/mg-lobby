package work.lclpnet.lobby.dev;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Formatting;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.kibu.plugin.ext.PluginContext;
import work.lclpnet.kibu.scheduler.Ticks;
import work.lclpnet.lobby.LobbyPlugin;
import work.lclpnet.lobby.game.api.GameEnvironment;
import work.lclpnet.lobby.game.api.GameInstance;
import work.lclpnet.lobby.game.api.GameStarter;
import work.lclpnet.lobby.game.start.ConditionGameStarter;

import java.util.function.BooleanSupplier;

public class TestGameInstance implements GameInstance {

    private final GameEnvironment environment;

    public TestGameInstance(GameEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public GameStarter createStarter(GameStarter.Args args, GameStarter.Callback onStart) {
        int minPlayers = !FabricLoader.getInstance().isDevelopmentEnvironment() ? 1 : 2;

        BooleanSupplier condition = () -> PlayerLookup.all(environment.getServer()).size() >= minPlayers;

        var starter = new ConditionGameStarter(condition, args, onStart, environment);

        // optionally, you can configure the starter:

        var translations = LobbyPlugin.getInstance().getTranslationService();

        // you can set a periodic condition message that gets sent to everyone, if the game cannot start.
        var notEnoughPlayers = translations.translateText("lobby.game.not_enough_players", minPlayers)
                .formatted(Formatting.RED);

        starter.setConditionMessage(notEnoughPlayers::translateFor);
        starter.setConditionCheckInterval(Ticks.seconds(20));

        // you can set a title that gets displayed as boss bar if the game cannot start
        starter.setConditionBossBarValue(translations.translateText("lobby.game.waiting_for_players"));

        return starter;
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
