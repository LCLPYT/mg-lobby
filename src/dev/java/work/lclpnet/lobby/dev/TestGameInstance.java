package work.lclpnet.lobby.dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.game.api.GameEnvironment;
import work.lclpnet.game.api.GameInstance;
import work.lclpnet.game.api.option.VoteResult;
import work.lclpnet.kibu.scheduler.Ticks;

import java.util.Comparator;
import java.util.Map.Entry;

public class TestGameInstance implements GameInstance {

    private static final Logger logger = LoggerFactory.getLogger(TestGameInstance.class);
    private final GameEnvironment environment;
    private final VoteResult<String> mapVotingResult;

    public TestGameInstance(GameEnvironment environment, VoteResult<String> mapVotingResult) {
        this.environment = environment;
        this.mapVotingResult = mapVotingResult;
    }

    @Override
    public void start() {
        System.out.println("The test game was started! (will end in 10 seconds)");

        System.out.println("Most voted map: " + mapVotingResult.getMostVoted());

        System.out.println("All map votes:");
        mapVotingResult.asMap().entrySet().stream()
                .sorted(Comparator.<Entry<String, Integer>>comparingInt(Entry::getValue).reversed())
                .forEach(entry
                        -> System.out.println(entry.getKey() + ": " + entry.getValue() + " votes"));

        TestGameActivity activity = new TestGameActivity(environment.getServer(), logger);

        environment.switchRootActivity(activity);

        environment.getSchedulerStack().timeout(() -> {
            System.out.println("The test game has ended!");
            environment.getFinisher().finishGame();
        }, Ticks.seconds(10));
    }
}
