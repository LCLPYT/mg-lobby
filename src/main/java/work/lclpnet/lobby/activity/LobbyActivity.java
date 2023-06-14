package work.lclpnet.lobby.activity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBuilder;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.config.LobbyConfig;
import work.lclpnet.lobby.decor.GeyserManager;
import work.lclpnet.lobby.decor.KingOfLadder;
import work.lclpnet.lobby.decor.jnr.JumpAndRun;
import work.lclpnet.lobby.decor.maze.LobbyMazeCreator;
import work.lclpnet.lobby.event.JumpAndRunListener;
import work.lclpnet.lobby.event.KingOfLadderListener;
import work.lclpnet.lobby.event.LobbyListener;
import work.lclpnet.lobby.util.ResetBlockWriter;

import static work.lclpnet.activity.component.builtin.BuiltinComponents.HOOKS;
import static work.lclpnet.activity.component.builtin.BuiltinComponents.SCHEDULER;

public class LobbyActivity extends ComponentActivity {

    private final LobbyManager lobbyManager;
    private final LobbyMazeCreator mazeCreator;
    private ResetBlockWriter blockWriter;
    private KingOfLadder kingOfLadder;
    private JumpAndRun jumpAndRun;

    public LobbyActivity(PluginContext context, LobbyManager lobbyManager) {
        super(context);
        this.lobbyManager = lobbyManager;
        this.mazeCreator = new LobbyMazeCreator(lobbyManager, lobbyManager.getLogger());
    }

    @Override
    protected void buildComponents(ComponentBuilder components) {
        components.add(HOOKS).add(SCHEDULER);
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
        blockWriter = new ResetBlockWriter(world);
        mazeCreator.create(blockWriter, world);

        // init king of the ladder
        LobbyConfig config = lobbyManager.getConfig();
        Scheduler scheduler = component(SCHEDULER).scheduler();

        if (config.kingOfLadderGoal != null) {
            kingOfLadder = new KingOfLadder(world, config.kingOfLadderGoal, config.kingOfLadderDisplays, lobbyManager.getTranslationService());
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
            jumpAndRun = new JumpAndRun(world, config.jumpAndRunStart, blockWriter);
            hooks.registerHooks(new JumpAndRunListener(jumpAndRun));
        }
    }

    @Override
    public void stop() {
        super.stop();

        blockWriter.undo();

        if (kingOfLadder != null) {
            kingOfLadder.reset();
        }
    }
}
