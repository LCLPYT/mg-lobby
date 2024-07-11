package work.lclpnet.lobby.config;

import org.jetbrains.annotations.NotNull;

public interface ConfigAccess {

    @NotNull
    LobbyConfig getConfig();

    @NotNull
    LobbyWorldConfig getWorldConfig();
}
