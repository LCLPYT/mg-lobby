package work.lclpnet.lobby;

import work.lclpnet.lobby.api.LobbyManager;

public interface LobbyAPI {

    LobbyManager getManager();

    void enterLobbyPhase();

    static LobbyAPI getInstance() {
        return LobbyPlugin.getInstance();
    }
}
