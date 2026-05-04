package work.lclpnet.lobby.game.map;

import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.gaco.asset.AssetPath;
import work.lclpnet.gaco.asset.UriAssetRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AssetMapRepositoryTest {

    static final Logger logger = LoggerFactory.getLogger("test");
    URI uri;
    AssetMapRepository repo;
    UriAssetRepository assetRepository;

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void setupEach() {
        uri = Path.of("src", "test", "resources", "maps").toUri();

        assetRepository = new UriAssetRepository(uri, logger);
        repo = new AssetMapRepository(assetRepository, Map.of(), logger);
    }

    @Test
    void testSimplePath() throws IOException {
        var maps = repo.getMapList(AssetPath.of("test"));

        assertEquals(Set.of("map_one", "map_two", "map_three"), maps.stream()
                .map(MapRef::path)
                .collect(Collectors.toSet()));
    }

    @Test
    void testNestedPath() throws IOException {
        var maps = repo.getMapList(AssetPath.of("foo"));

        assertEquals(Set.of("bar/baz", "bar/hi"), maps.stream()
                .map(MapRef::path)
                .collect(Collectors.toSet()));
    }

    @Test
    void testAbsolutePath() throws IOException {
        var maps = repo.getMapList(AssetPath.of("my_collection"));

        assertEquals(Set.of("/test/map_two"), maps.stream()
                .map(MapRef::path)
                .collect(Collectors.toSet()));
    }

    @Test
    void testInfoSimple() throws IOException {
        var info = repo.getMapInfo(AssetPath.of("test/map_three"));

        assertEquals("test/map_three", info.target());
        assertEquals("world.tar.xz", info.properties().get("source"));
        assertSame(repo, info.origin());
    }

    @Test
    void testInfoPropertiesMerged() throws IOException {
        var info = repo.getMapInfo(AssetPath.of("test/map_one"));

        assertEquals(true, info.properties().get("extraProp"));
    }

    @Test
    void testInfoLink() throws IOException {
        var info = repo.getMapInfo(AssetPath.of("linked/test"));

        assertEquals("test/map_three", info.target());
    }

    @Test
    void testInfoLinkDataInherited() throws IOException {
        var info = repo.getMapInfo(AssetPath.of("linked/with_data"));

        assertEquals(10, info.properties().get("inheritedProp"));
        assertEquals(true, info.properties().get("extraProp"));
    }

    @Test
    void testInfoLinkRelative() throws IOException {
        var info = repo.getMapInfo(AssetPath.of("linked/relative"));

        assertEquals("linked/relative/map", info.target());
        assertEquals("here", info.properties().get("source"));
    }

    @Test
    void testInfoLinkRelativeUp() throws IOException {
        var info = repo.getMapInfo(AssetPath.of("linked/relative/up"));

        assertEquals("linked/relative/map", info.target());
        assertEquals("here", info.properties().get("source"));
        assertEquals(123, info.properties().get("test"));
    }

    @Test
    void testInfoLinkMaxDepthExceededThrows() {
        assertThrows(IOException.class, () -> repo.getMapInfo(AssetPath.of("cycle/map_a")));
    }

    @Test
    void testInfoLinkTargetPropertyRemoved() throws IOException {
        var info = repo.getMapInfo(AssetPath.of("linked/test"));

        assertNull(info.properties().opt("target"));
    }

    @Test
    void testInfoLinkOutsideOfRepoThrows() {
        assertThrows(IOException.class, () -> repo.getMapInfo(AssetPath.of("broken/escape")));
    }

    @Test
    void testGetMapsPathOutsideOfRepoThrows() {
        assertThrows(IOException.class, () -> repo.getMapList(AssetPath.of("../escaped")));
    }

    @Test
    void testOpenBasic() throws IOException {
        consume(repo.open(AssetPath.of("test/map_two/world.zip")));
    }

    @Test
    void testOpenRelative() throws IOException {
        consume(repo.open(AssetPath.of("test/map_two/../map_three/world.tar.xz")));
    }

    @Test
    void testOpenAbsolute() throws IOException {
        consume(repo.open(AssetPath.of("/test/map_three/world.tar.xz")));
    }

    @Test
    void testOpenPathOutsideThrows() {
        assertThrows(IOException.class, () -> consume(repo.open(AssetPath.of("../test/map_two/world.tar"))),
                "Path outside of repository");
    }

    @Test
    void testOpenResourceOutsideThrows() {
        assertThrows(IOException.class, () -> consume(repo.open(AssetPath.of("test/map_two/../../../world.tar"))),
                "Path outside of repository");
    }

    @Test
    void testOpenAbsoluteResourceOutsideEmpty() {
        assertThrows(IOException.class, () -> consume(repo.open(AssetPath.of("test/map_two/../../../world.tar"))),
                "Path outside of repository");
    }

    @Test
    void testConstraintsNoDepsAlwaysIncluded() throws IOException, VersionParsingException {
        var versionedRepo = new AssetMapRepository(assetRepository,
                Map.of("test-mod", Version.parse("1.5.0"), "other-mod", Version.parse("3.2.0")), logger);
        var maps = versionedRepo.getMapList(AssetPath.of("constrained"));
        assertTrue(maps.stream().anyMatch(m -> m.path().equals("map_no_deps")));
    }

    @Test
    void testConstraintsSatisfiedIncluded() throws IOException, VersionParsingException {
        var versionedRepo = new AssetMapRepository(assetRepository,
                Map.of("test-mod", Version.parse("1.5.0"), "other-mod", Version.parse("3.2.0")), logger);
        var paths = versionedRepo.getMapList(AssetPath.of("constrained")).stream()
                .map(MapRef::path).collect(Collectors.toSet());
        assertTrue(paths.contains("map_satisfied"));
    }

    @Test
    void testConstraintsUnsatisfiedExcluded() throws IOException, VersionParsingException {
        var versionedRepo = new AssetMapRepository(assetRepository,
                Map.of("test-mod", Version.parse("1.5.0"), "other-mod", Version.parse("3.2.0")), logger);
        var paths = versionedRepo.getMapList(AssetPath.of("constrained")).stream()
                .map(MapRef::path).collect(Collectors.toSet());
        assertFalse(paths.contains("map_unsatisfied"));
    }

    @Test
    void testConstraintsMissingModuleExcluded() throws IOException, VersionParsingException {
        var versionedRepo = new AssetMapRepository(assetRepository,
                Map.of("test-mod", Version.parse("1.5.0"), "other-mod", Version.parse("3.2.0")), logger);
        var paths = versionedRepo.getMapList(AssetPath.of("constrained")).stream()
                .map(MapRef::path).collect(Collectors.toSet());
        assertFalse(paths.contains("map_missing_module"));
    }

    @Test
    void testConstraintsMultiDepAllSatisfiedIncluded() throws IOException, VersionParsingException {
        var versionedRepo = new AssetMapRepository(assetRepository,
                Map.of("test-mod", Version.parse("1.5.0"), "other-mod", Version.parse("3.2.0")), logger);
        var paths = versionedRepo.getMapList(AssetPath.of("constrained")).stream()
                .map(MapRef::path).collect(Collectors.toSet());
        assertTrue(paths.contains("map_multi_dep_satisfied"));
    }

    @Test
    void testConstraintsMultiDepOneUnsatisfiedExcluded() throws IOException, VersionParsingException {
        var versionedRepo = new AssetMapRepository(assetRepository,
                Map.of("test-mod", Version.parse("1.5.0"), "other-mod", Version.parse("3.2.0")), logger);
        var paths = versionedRepo.getMapList(AssetPath.of("constrained")).stream()
                .map(MapRef::path).collect(Collectors.toSet());
        assertFalse(paths.contains("map_multi_dep_one_unsatisfied"));
    }

    @Test
    void testConstraintsNoModulesExcludesAllWithDeps() throws IOException {
        var paths = repo.getMapList(AssetPath.of("constrained")).stream()
                .map(MapRef::path).collect(Collectors.toSet());
        assertEquals(Set.of("map_no_deps"), paths);
    }

    @Test
    void testLegacyConstraintIncludedOnOlderVersion() throws IOException, VersionParsingException {
        var versionedRepo = new AssetMapRepository(assetRepository,
                Map.of("test-mod", Version.parse("1.5.0")), logger);
        var paths = versionedRepo.getMapList(AssetPath.of("constrained")).stream()
                .map(MapRef::path).collect(Collectors.toSet());
        assertTrue(paths.contains("map_legacy"));
    }

    @Test
    void testLegacyConstraintExcludedOnNewerVersion() throws IOException, VersionParsingException {
        var versionedRepo = new AssetMapRepository(assetRepository,
                Map.of("test-mod", Version.parse("2.0.0")), logger);
        var paths = versionedRepo.getMapList(AssetPath.of("constrained")).stream()
                .map(MapRef::path).collect(Collectors.toSet());
        assertFalse(paths.contains("map_legacy"));
    }

    private void consume(InputStream in) throws IOException {
        try (in) {
            assertTrue(in.readAllBytes().length > 0);
        }
    }
}