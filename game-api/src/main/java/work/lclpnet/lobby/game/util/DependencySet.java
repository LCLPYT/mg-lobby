package work.lclpnet.lobby.game.util;

import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A set of version constraints.
 * @param constraints The constraints.
 */
public record DependencySet(Map<String, Collection<VersionPredicate>> constraints) {

    public boolean matches(Map<String, Version> versions, Consumer<String> reporter) {
        for (var constraint : constraints.entrySet()) {
            String id = constraint.getKey();
            var predicates = constraint.getValue();

            if (predicates.isEmpty()) continue;

            Version version = versions.getOrDefault(id, null);

            if (version == null) {
                reporter.accept("Module %s is not present".formatted(id));
                return false;
            }

            for (VersionPredicate predicate : predicates) {
                if (predicate.test(version)) continue;

                reporter.accept("Module %s needs to match %s but is %s".formatted(id, predicate, version));
                return false;
            }
        }

        return true;
    }

    public static DependencySet create(Map<String, ?> map, Logger logger) {
        Map<String, Collection<VersionPredicate>> predicates = new LinkedHashMap<>(map.size());

        for (var entry : map.entrySet()) {
            String id = entry.getKey();

            Object value = entry.getValue();

            if (!(value instanceof String str)) {
                logger.error("Expected a string value in dependency set entry: {} => {}", id, value);
                continue;
            }

            try {
                VersionPredicate predicate = VersionPredicate.parse(str);

                predicates.put(id, List.of(predicate));
            } catch (VersionParsingException e) {
                logger.error("Invalid version constraint: {}", str, e);
            }
        }

        return new DependencySet(predicates);
    }

    public interface Reporter {
        void report(String id, @Nullable Version current, String msg);
    }
}
