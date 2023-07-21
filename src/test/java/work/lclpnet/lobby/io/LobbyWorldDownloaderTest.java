package work.lclpnet.lobby.io;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import work.lclpnet.lobby.config.ConfigAccess;
import work.lclpnet.lobby.config.LobbyConfig;
import work.lclpnet.lobby.config.LobbyWorldConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LobbyWorldDownloaderTest {

    private static final ConfigAccess TEST_CONFIG_ACCESS = new ConfigAccess() {
        @NotNull
        @Override
        public LobbyConfig getConfig() {
            LobbyConfig lobbyConfig = new LobbyConfig();
            lobbyConfig.lobbySource = Path.of("src", "test", "resources", "directory.tar.gz").toUri();
            return lobbyConfig;
        }

        @NotNull
        @Override
        public LobbyWorldConfig getWorldConfig() {
            throw new AssertionError();
        }
    };

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void testRenewLobbyEmpty() throws IOException {
        Path lobbyDir = Files.createTempDirectory("mgl_lwr").resolve("lobby");
        assertFalse(Files.exists(lobbyDir));

        LobbyWorldDownloader reset = new LobbyWorldDownloader(lobbyDir, TEST_CONFIG_ACCESS);
        reset.renewWorld();

        assertTrue(Files.exists(lobbyDir));
    }

    @Test
    void testRenewLobbyExisting() throws IOException {
        Path lobbyDir = Files.createTempDirectory("mgl_lwr").resolve("lobby");
        assertFalse(Files.exists(lobbyDir));

        // populate lobby dir with some junk
        Path nestedDir = lobbyDir.resolve("test");
        Files.createDirectories(nestedDir);
        Files.writeString(lobbyDir.resolve("test.txt"), "Hello World", StandardCharsets.UTF_8);
        Files.writeString(lobbyDir.resolve("test").resolve("foo.txt"), "Foo bar baz", StandardCharsets.UTF_8);
        assertTrue(Files.exists(lobbyDir));

        // reset the lobby
        LobbyWorldDownloader reset = new LobbyWorldDownloader(lobbyDir, TEST_CONFIG_ACCESS);
        reset.renewWorld();

        assertTrue(Files.exists(lobbyDir));

        // verify the junk content was deleted
        assertFalse(Files.exists(nestedDir));
        DirectoryWorldCopierTest.assertIsTestContent(lobbyDir);  // will verify exact file count
    }
}