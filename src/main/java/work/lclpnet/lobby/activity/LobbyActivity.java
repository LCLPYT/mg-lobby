package work.lclpnet.lobby.activity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.event.LobbyListener;
import work.lclpnet.lobby.maze.LobbyMazeCreator;
import work.lclpnet.lobby.maze.ResetBlockWriter;

public class LobbyActivity implements Activity {

    private final LobbyManager lobbyManager;
    private final LobbyMazeCreator mazeCreator;
    private ResetBlockWriter blockWriter;

    public LobbyActivity(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
        this.mazeCreator = new LobbyMazeCreator(lobbyManager, lobbyManager.getLogger());
    }

    @Override
    public void startActivity(PluginContext context) {
        context.registerHooks(new LobbyListener(lobbyManager));

        MinecraftServer server = context.getEnvironment().getServer();

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
    public void endActivity(PluginContext context) {
        blockWriter.undo();
    }
}
