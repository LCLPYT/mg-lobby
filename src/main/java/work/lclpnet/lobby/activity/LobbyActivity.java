package work.lclpnet.lobby.activity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.kibu.plugin.cmd.CommandRegistrar;
import work.lclpnet.kibu.plugin.ext.PluginContext;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.cmd.PauseCommand;
import work.lclpnet.lobby.cmd.ResumeCommand;
import work.lclpnet.lobby.cmd.SetGameCommand;
import work.lclpnet.lobby.cmd.StartCommand;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.decor.GeyserManager;
import work.lclpnet.lobby.decor.KingOfLadder;
import work.lclpnet.lobby.decor.ttt.TicTacToeManager;
import work.lclpnet.lobby.di.ActivityComponent;
import work.lclpnet.lobby.di.ActivityModule;
import work.lclpnet.lobby.game.Game;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.lobby.game.start.GameStarter;
import work.lclpnet.lobby.service.SyncActivityManager;
import work.lclpnet.lobby.util.ResetWorldModifier;

import javax.inject.Inject;
import java.util.function.Supplier;

import static work.lclpnet.activity.component.builtin.BuiltinComponents.*;

public class LobbyActivity extends ComponentActivity {

    private final LobbyManager lobbyManager;
    private final ActivityManager childActivity;
    private final ActivityComponent.Builder componentBuilder;
    private GameStarter gameStarter;
    private ResetWorldModifier worldModifier;
    private KingOfLadder kingOfLadder;
    private TicTacToeManager ticTacToeManager;
    private ActivityComponent component;

    @Inject
    public LobbyActivity(PluginContext context, LobbyManager lobbyManager, ActivityComponent.Builder componentBuilder) {
        super(context);
        this.lobbyManager = lobbyManager;
        this.childActivity = new SyncActivityManager();
        this.componentBuilder = componentBuilder;
    }

    @Override
    protected void registerComponents(ComponentBundle components) {
        components.add(HOOKS).add(SCHEDULER).add(COMMANDS);
    }

    @Override
    public void start() {
        super.start();

        HookRegistrar hooks = component(HOOKS).hooks();
        Scheduler scheduler = component(SCHEDULER).scheduler();
        CommandRegistrar commands = component(COMMANDS).commands();

        MinecraftServer server = getServer();

        component = componentBuilder
                .activityModule(new ActivityModule(hooks, scheduler, server))
                .build();

        hooks.registerHooks(component.lobbyListener());

        // send every online player to the lobby
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            lobbyManager.sendToLobby(player);
        }

        worldModifier = component.worldModifier();

        // generate maze
        component.mazeGenerator().create();

        // init king of the ladder
        LobbyWorldConfig config = lobbyManager.getWorldConfig();

        if (config.kingOfLadderGoal != null) {
            kingOfLadder = component.kingOfLadder();
            hooks.registerHooks(component.kingOfLadderListener());
            scheduler.interval(kingOfLadder::tick, 6);
        }

        // init geysers
        if (config.geysers != null) {
            GeyserManager geyserManager = component.geyserManager();
            scheduler.interval(geyserManager::tick, 1);
        }

        // jump and run
        if (config.jumpAndRunStart != null) {
            hooks.registerHooks(component.jumpAndRunListener());
        }

        // init seat handler
        component.seatHandler().init();

        // tic tac toe
        ticTacToeManager = component.ticTacToeManager();
        hooks.registerHooks(component.ticTacToeListener());

        // game stuff
        final GameManager gameManager = lobbyManager.getGameManager();

        Supplier<GameStarter> gameStarterSupplier = () -> gameStarter;

        new StartCommand(gameStarterSupplier).register(commands);
        new SetGameCommand(gameManager, this::changeGame).register(commands);
        new PauseCommand(gameStarterSupplier).register(commands);
        new ResumeCommand(gameStarterSupplier).register(commands);

        changeGame(gameManager.getCurrentGame());
    }

    private void changeGame(Game game) {
        if (gameStarter != null) {
            gameStarter.destroy();
        }

        lobbyManager.getGameManager().setCurrentGame(game);

        if (game == null) return;

        this.gameStarter = component.gameStarter().create(childActivity, game);
        this.gameStarter.init();
    }

    @Override
    public void stop() {
        super.stop();

        worldModifier.undo();

        if (kingOfLadder != null) {
            kingOfLadder.reset();
        }

        ticTacToeManager.reset();

        childActivity.stop();
    }
}
