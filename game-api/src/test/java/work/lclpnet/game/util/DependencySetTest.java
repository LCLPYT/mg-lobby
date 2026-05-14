package work.lclpnet.game.util;

import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.game.util.DependencySet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DependencySetTest {

    static final Logger logger = LoggerFactory.getLogger("test");

    @Test
    void testEmptyConstraintsAlwaysMatch() {
        var deps = new DependencySet(Map.of());
        assertTrue(deps.matches(Map.of(), msg -> {}));
    }

    @Test
    void testSingleConstraintSatisfied() throws VersionParsingException {
        var deps = DependencySet.create(Map.of("mod-a", ">=1.0.0"), logger);
        assertTrue(deps.matches(Map.of("mod-a", Version.parse("1.5.0")), msg -> {}));
    }

    @Test
    void testSingleConstraintVersionTooLow() throws VersionParsingException {
        var deps = DependencySet.create(Map.of("mod-a", ">=2.0.0"), logger);
        assertFalse(deps.matches(Map.of("mod-a", Version.parse("1.5.0")), msg -> {}));
    }

    @Test
    void testMissingModuleReturnsFalse() {
        var deps = DependencySet.create(Map.of("mod-a", ">=1.0.0"), logger);
        assertFalse(deps.matches(Map.of(), msg -> {}));
    }

    @Test
    void testMissingModuleReportsError() {
        var deps = DependencySet.create(Map.of("mod-a", ">=1.0.0"), logger);
        List<String> reports = new ArrayList<>();
        deps.matches(Map.of(), reports::add);
        assertFalse(reports.isEmpty());
        assertTrue(reports.get(0).contains("mod-a"));
    }

    @Test
    void testVersionMismatchReportsError() throws VersionParsingException {
        var deps = DependencySet.create(Map.of("mod-a", ">=2.0.0"), logger);
        List<String> reports = new ArrayList<>();
        deps.matches(Map.of("mod-a", Version.parse("1.0.0")), reports::add);
        assertFalse(reports.isEmpty());
        assertTrue(reports.get(0).contains("mod-a"));
    }

    @Test
    void testMultipleConstraintsAllSatisfied() throws VersionParsingException {
        var deps = DependencySet.create(Map.of("mod-a", ">=1.0.0", "mod-b", "^2.0.0"), logger);
        var versions = Map.of(
                "mod-a", Version.parse("1.5.0"),
                "mod-b", Version.parse("2.3.0")
        );
        assertTrue(deps.matches(versions, msg -> {}));
    }

    @Test
    void testMultipleConstraintsOneFails() throws VersionParsingException {
        var deps = DependencySet.create(Map.of("mod-a", ">=1.0.0", "mod-b", "^3.0.0"), logger);
        var versions = Map.of(
                "mod-a", Version.parse("1.5.0"),
                "mod-b", Version.parse("2.3.0")
        );
        assertFalse(deps.matches(versions, msg -> {}));
    }

    @Test
    void testEmptyPredicateListSkipped() throws VersionParsingException {
        // DependencySet with a key that has an empty predicate collection should not fail
        // even if the module is absent — empty predicate list is ignored per implementation
        var deps = new DependencySet(Map.of("absent-mod", List.of()));
        assertTrue(deps.matches(Map.of(), msg -> {}));
    }

    @Test
    void testExactVersionMatch() throws VersionParsingException {
        var deps = DependencySet.create(Map.of("mod-a", "1.2.3"), logger);
        assertTrue(deps.matches(Map.of("mod-a", Version.parse("1.2.3")), msg -> {}));
        assertFalse(deps.matches(Map.of("mod-a", Version.parse("1.2.4")), msg -> {}));
    }

    @Test
    void testNonStringValueSkipped() {
        // non-string value in the map should not throw
        var deps = DependencySet.create(Map.of("mod-a", 42), logger);
        assertTrue(deps.matches(Map.of(), msg -> {}));
    }
}
