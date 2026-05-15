package work.lclpnet.lobby.dev;

import org.jetbrains.annotations.NotNull;
import work.lclpnet.game.api.GameEnvironment;
import work.lclpnet.game.api.GameInstance;
import work.lclpnet.game.api.option.GameOptions;
import work.lclpnet.kibu.scheduler.Ticks;

public class AnotherGameInstance implements GameInstance {

    private final GameEnvironment env;

    public AnotherGameInstance(GameEnvironment env) {
        this.env = env;
    }

    @Override
    public void start(@NotNull GameOptions options) {
        // just end the game after 10s
        env.getSchedulerStack().timeout(Ticks.seconds(10), () -> env.getFinisher().finishGame());
    }
}
