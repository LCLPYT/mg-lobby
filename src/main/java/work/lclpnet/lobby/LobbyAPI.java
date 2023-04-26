package work.lclpnet.lobby;

import work.lclpnet.lobby.api.LobbyManager;

public interface LobbyAPI {

    LobbyManager getManager();

    static LobbyAPI getInstance() {
        return LobbyPlugin.getInstance();
    }
}
