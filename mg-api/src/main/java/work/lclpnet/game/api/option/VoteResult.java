package work.lclpnet.game.api.option;

import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.max;

public interface VoteResult<T> {

    Map<T, Integer> asMap();

    default int votes(T option) {
        return max(0, asMap().getOrDefault(option, 0));
    }

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

    static <T> VoteResult<T> empty() {
        return Map::of;
    }
}
