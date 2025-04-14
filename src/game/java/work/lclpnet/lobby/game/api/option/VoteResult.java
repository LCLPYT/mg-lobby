package work.lclpnet.lobby.game.api.option;

import java.util.*;
import java.util.stream.Collectors;

public interface VoteResult<T> {

    Map<T, Integer> asMap();

    default Set<T> getMostVoted() {
        Map<T, Integer> map = asMap();

        OptionalInt maxVotes = map.values().stream()
                .mapToInt(Integer::intValue)
                .max();

        if (maxVotes.isEmpty()) {
            return Set.of();
        }

        int maxVoteCount = maxVotes.getAsInt();

        return map.entrySet().stream()
                .filter(entry -> entry.getValue() == maxVoteCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
