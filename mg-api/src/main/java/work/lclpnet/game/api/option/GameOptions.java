package work.lclpnet.game.api.option;

import java.util.Optional;

public interface GameOptions {

    <T> Optional<VoteResult<T>> getVotingResults(String name, Class<T> optionType);
}
