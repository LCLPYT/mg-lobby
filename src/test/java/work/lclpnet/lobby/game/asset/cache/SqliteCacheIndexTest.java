package work.lclpnet.lobby.game.asset.cache;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SqliteCacheIndexTest {

    static final Logger logger = LoggerFactory.getLogger(SqliteCacheIndexTest.class);

    @BeforeAll
    static void setupJdbc() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC", true, SqliteCacheIndex.class.getClassLoader());
    }

    @Test
    void getExpiryTimestamp_exists() throws SQLException, IOException {
        Path path = Path.of("src", "test", "resources", "cache", "index_simple_entry.sqlite");

        try (var index = SqliteCacheIndex.createSqliteIndex(path, logger)) {
            assertEquals(1720210276, index.getExpiryTimestamp("test/hello"));
        }
    }

    @Test
    void isEntryInvalid_expired_true() throws SQLException, IOException {
        Path path = Path.of("src", "test", "resources", "cache", "index_simple_entry.sqlite");

        try (var index = SqliteCacheIndex.createSqliteIndex(path, logger)) {
            assertTrue(index.isEntryInvalid("test/hello"));
        }
    }

    @Nested
    class Fresh {
        SqliteCacheIndex index;

        @BeforeEach
        void setUp() throws IOException, SQLException {
            Path path = Files.createTempFile("mgl_sci", "index.sqlite");

            // this executes SqliteCacheMigration internally
            index = SqliteCacheIndex.createSqliteIndex(path, logger);
        }

        @AfterEach
        void tearDown() throws IOException {
            index.close();
        }

        @Test
        void getTimestamp() {
            assertEquals(-1, index.getExpiryTimestamp("test/hello"));
        }

        @Test
        void updateEntry() {
            String path = "test/hello";

            long now = System.currentTimeMillis() / 1000;

            index.updateEntry(path, 3600);

            long expiryTime = index.getExpiryTimestamp(path);

            assertTrue(expiryTime >= now + 3600);
        }

        @Test
        void isEntryInvalid_doesNotExist_true() {
            assertTrue(index.isEntryInvalid("test/hello"));
        }

        @Test
        void isEntryInvalid_beforeExpiry_false() {
            String path = "test/hello";

            index.updateEntry(path, 1800);

            assertFalse(index.isEntryInvalid(path));
        }

        @Test
        void invalidate_cached_isInvalidated() {
            String path = "test/hello";
            index.updateEntry(path, 10000);

            assertFalse(index.isEntryInvalid(path));

            index.invalidate(path);

            assertTrue(index.isEntryInvalid(path));
        }

        @Test
        void invalidate_notExisting_noop() {
            String key = "test123";

            assertTrue(index.isEntryInvalid(key));
            index.invalidate(key);  // TODO test result
        }
    }
}