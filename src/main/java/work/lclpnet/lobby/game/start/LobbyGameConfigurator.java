package work.lclpnet.lobby.game.start;

import org.jetbrains.annotations.Nullable;
import work.lclpnet.lobby.activity.LobbyActivity;

import java.util.function.Consumer;

public class LobbyGameConfigurator {

    @Nullable
    private LobbyActivity activity = null;

    public void configure(Consumer<LobbyActivity> ifActive) {
        synchronized (this) {
            ifActive.accept(activity);
        }
    }

    public void setActivity(@Nullable LobbyActivity activity) {
        synchronized (this) {
            this.activity = activity;
        }
    }
}
