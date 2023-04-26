package work.lclpnet.lobby.activity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.event.LobbyListener;

public class LobbyActivity implements Activity {

    private final LobbyManager lobbyManager;

    public LobbyActivity(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @Override
    public void startActivity(PluginContext context) {
        context.registerHooks(new LobbyListener(lobbyManager));

        MinecraftServer server = context.getEnvironment().getServer();

        // send every online player to the lobby
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            lobbyManager.sendToLobby(player);
        }
    }

    @Override
    public void endActivity(PluginContext context) {

    }
}
