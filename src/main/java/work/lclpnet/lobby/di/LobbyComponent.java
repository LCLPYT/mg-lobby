package work.lclpnet.lobby.di;

import dagger.Component;
import work.lclpnet.lobby.LobbyManagerImpl;
import work.lclpnet.lobby.activity.LobbyActivity;
import work.lclpnet.lobby.io.LobbyWorldDownloader;
import work.lclpnet.lobby.io.ServerPropertiesAdjuster;

import javax.inject.Singleton;

@Singleton
@Component(modules = LobbyModule.class)
public interface LobbyComponent {

    LobbyManagerImpl lobbyManager();

    ServerPropertiesAdjuster serverPropertiesAdjuster();

    LobbyWorldDownloader lobbyWorldDownloader();

    LobbyActivity lobbyActivity();

    // subcomponents
    ActivityComponent.Builder activityComponent();
}
