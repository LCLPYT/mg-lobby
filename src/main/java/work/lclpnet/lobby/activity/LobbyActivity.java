package work.lclpnet.lobby.activity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.api.activity.ComponentActivity;
import work.lclpnet.lobby.api.component.ComponentBundle;
import work.lclpnet.lobby.event.LobbyListener;
import work.lclpnet.lobby.maze.LobbyMazeCreator;
import work.lclpnet.lobby.maze.ResetBlockWriter;

import static work.lclpnet.lobby.api.component.builtin.BuiltinComponents.HOOKS;

public class LobbyActivity extends ComponentActivity {

    private final LobbyManager lobbyManager;
    private final LobbyMazeCreator mazeCreator;
    private ResetBlockWriter blockWriter;

    public LobbyActivity(PluginContext context, LobbyManager lobbyManager) {
        super(context);
        this.lobbyManager = lobbyManager;
        this.mazeCreator = new LobbyMazeCreator(lobbyManager, lobbyManager.getLogger());
    }

    @Override
    protected void initComponents(ComponentBundle components) {
        components.add(HOOKS);
    }

    @Override
    public void start() {
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
    }

    @Override
    public void stop() {
        blockWriter.undo();
    }
}
