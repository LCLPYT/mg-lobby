package work.lclpnet.lobby.dev;

import work.lclpnet.kibu.scheduler.Ticks;
import work.lclpnet.lobby.game.api.GameEnvironment;
import work.lclpnet.lobby.game.api.GameInstance;
import work.lclpnet.lobby.game.api.option.GameOptions;

public class AnotherGameInstance implements GameInstance {

    private final GameEnvironment env;

    public AnotherGameInstance(GameEnvironment env) {
        this.env = env;
    }

    @Override
    public void start(GameOptions options) {
        // just end the game after 10s
        env.getSchedulerStack().timeout(Ticks.seconds(10), () -> env.getFinisher().finishGame());
    }
}
