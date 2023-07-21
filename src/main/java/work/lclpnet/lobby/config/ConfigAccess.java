package work.lclpnet.lobby.config;

import javax.annotation.Nonnull;

public interface ConfigAccess {

    @Nonnull
    LobbyConfig getConfig();

    @Nonnull
    LobbyWorldConfig getWorldConfig();
}
