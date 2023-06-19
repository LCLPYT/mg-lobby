package work.lclpnet.lobby.activity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.manager.ActivityManager;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.kibu.plugin.cmd.CommandRegistrar;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.cmd.SetGameCommand;
import work.lclpnet.lobby.cmd.StartCommand;
import work.lclpnet.lobby.config.LobbyConfig;
import work.lclpnet.lobby.decor.GeyserManager;
import work.lclpnet.lobby.decor.KingOfLadder;
import work.lclpnet.lobby.decor.jnr.JumpAndRun;
import work.lclpnet.lobby.decor.maze.LobbyMazeCreator;
import work.lclpnet.lobby.event.JumpAndRunListener;
import work.lclpnet.lobby.event.KingOfLadderListener;
import work.lclpnet.lobby.event.LobbyListener;
import work.lclpnet.lobby.game.Game;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.lobby.game.start.DefaultGameStarter;
import work.lclpnet.lobby.game.start.GameStarter;
import work.lclpnet.lobby.service.SyncActivityManager;
import work.lclpnet.lobby.service.TranslationService;
import work.lclpnet.lobby.util.ResetWorldModifier;

import static work.lclpnet.activity.component.builtin.BuiltinComponents.*;

public class LobbyActivity extends ComponentActivity {

    private final LobbyManager lobbyManager;
    private final LobbyMazeCreator mazeCreator;
    private final ActivityManager childActivity;
    private final PluginContext context;
    private GameStarter gameStarter;
    private ResetWorldModifier worldModifier;
    private KingOfLadder kingOfLadder;

    public LobbyActivity(PluginContext context, LobbyManager lobbyManager) {
        super(context);
        this.lobbyManager = lobbyManager;
        this.mazeCreator = new LobbyMazeCreator(lobbyManager, lobbyManager.getLogger());
        this.childActivity = new SyncActivityManager();
        this.context = context;
    }

    @Override
    protected void buildComponents(ComponentBundle components) {
        components.add(HOOKS).add(SCHEDULER).add(COMMANDS);
    }

    @Override
    public void start() {
        super.start();

        HookRegistrar hooks = component(HOOKS).hooks();
        hooks.registerHooks(new LobbyListener(lobbyManager));

        MinecraftServer server = getServer();

        // send every online player to the lobby
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            lobbyManager.sendToLobby(player);
        }

        // generate maze
        ServerWorld world = lobbyManager.getLobbyWorld();
        worldModifier = new ResetWorldModifier(world, hooks);
        mazeCreator.create(worldModifier, world);

        // init king of the ladder
        LobbyConfig config = lobbyManager.getConfig();
        Scheduler scheduler = component(SCHEDULER).scheduler();

        final TranslationService translationService = lobbyManager.getTranslationService();

        if (config.kingOfLadderGoal != null) {
            kingOfLadder = new KingOfLadder(world, config.kingOfLadderGoal, config.kingOfLadderDisplays, translationService);
            hooks.registerHooks(new KingOfLadderListener(kingOfLadder));
            scheduler.interval(kingOfLadder::tick, 6);
        }

        // init geysers
        if (config.geysers != null) {
            GeyserManager geyserManager = new GeyserManager(world, config.geysers);
            scheduler.interval(geyserManager::tick, 1);
        }

        // jump and run
        if (config.jumpAndRunStart != null) {
            JumpAndRun jumpAndRun = new JumpAndRun(world, config.jumpAndRunStart, worldModifier, scheduler, translationService);
            hooks.registerHooks(new JumpAndRunListener(jumpAndRun));
        }

        // game stuff
        final GameManager gameManager = lobbyManager.getGameManager();
        final CommandRegistrar commands = component(COMMANDS).commands();

        new StartCommand(() -> gameStarter).register(commands);
        new SetGameCommand(gameManager, this::changeGame).register(commands);

        changeGame(gameManager.getCurrentGame());
    }

    private void changeGame(Game game) {
        if (gameStarter != null) {
            gameStarter.destroy();
        }

        lobbyManager.getGameManager().setCurrentGame(game);

        if (game == null) return;

        final HookRegistrar hooks = component(HOOKS).hooks();

        this.gameStarter = new DefaultGameStarter(context, hooks, childActivity, game);
        this.gameStarter.init();
    }

    @Override
    public void stop() {
        super.stop();

        worldModifier.undo();

        if (kingOfLadder != null) {
            kingOfLadder.reset();
        }

        childActivity.stop();
    }
}
